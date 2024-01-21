/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import pl.edu.icm.trurl.ecs.Step;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Status {
    private final String text;
    private final int skip;
    private int counter;
    private final long startTime;
    public static StatusListener statusListener;
    public final ConcurrentHashMap<String, Integer> problems = new ConcurrentHashMap<>();

    public Status(String text, int skip) {
        this.text = text;
        this.skip = skip;
        if (skip <= 0) {
            throw new IllegalArgumentException("skip must be 1 or more");
        }
        startTime = System.currentTimeMillis();
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
        System.out.printf(" [OK] (in %f seconds)", duration / 1000.0);
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
            System.out.printf(" [PROBLEM] %s (%d time(s))%n", problem.getKey(), problem.getValue());
        }
    }

    public static Status of(String message) {
        return new Status(message, 100000);
    }

    public static Status of(String message, int skip) {
        return new Status(message, skip);
    }

    public static Step of(Step step, String message) {
        return sessionFactory -> {
            Status status = Status.of(message);
            step.execute(sessionFactory);
            status.done();
        };
    }
}
