/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.util;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Status {
    private final String text;
    private final int skip;
    private int counter;
    private long startTime;
    public static StatusListener statusListener;
    public final ConcurrentHashMap<String, Integer> problems = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String, TotalCounter> totalCounters = new ConcurrentHashMap<String, TotalCounter>();

    private final TotalCounter myCounter;

    private static class TotalCounter {
        private final AtomicDouble totalTime = new AtomicDouble();
        private final AtomicInteger executions = new AtomicInteger();
    }

    public Status(String text, int skip) {
        this.text = text;
        this.skip = skip;
        Preconditions.checkState(skip > 0, "skip must be 1 or more");
        startTime = System.currentTimeMillis();
        this.myCounter = totalCounters.computeIfAbsent(text, (unused) -> new TotalCounter());
        System.out.print(text + ": ");
    }

    public void tick() {
        counter++;
        while (counter > skip) {
            counter -= skip;
            System.out.print("#");
        }
    }

    public void done() {
        done(null);
    }

    public void problem(String problem) {
        problems.compute(problem, (k, v) -> v == null ? 1 : v + 1);
    }

    public void done(String comment, Object... args) {
        double now = System.currentTimeMillis();
        double duration = now - startTime;
        int executions = myCounter.executions.incrementAndGet();  // theoretically a race condition
        double totalTime = myCounter.totalTime.addAndGet(duration); // but doesn't matter in this case

        String totalComment = "";
        if (myCounter.executions.get() > 1) {
            totalComment = String.format(" (executed %d times, %f seconds on avg)", executions, totalTime / executions / 1000.0);
        }
        System.out.print(String.format(" [OK] (in %f seconds)%s", duration / 1000.0, totalComment));
        if (comment != null) {
            System.out.print(" -> " + String.format(comment, args));
        }
        System.out.println();
        if (statusListener != null) {
            statusListener.done((long) duration, text, comment != null ? String.format(comment, args) : null);
        }
        for (Map.Entry<String, Integer> problem : problems.entrySet()) {
            if (statusListener != null) {
                statusListener.problem(problem.getKey(), problem.getValue());
            }
            System.out.println(String.format(" [PROBLEM] %s (%d time(s))", problem.getKey(), problem.getValue()));
        }
    }

    public static Status of(String message) {
        return new Status(message, 100000);
    }

    public static Status of(String message, int skip) {
        return new Status(message, skip);
    }
}
