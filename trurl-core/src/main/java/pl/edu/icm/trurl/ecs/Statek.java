/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.ecs;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Statek {

    final FabrykaStrzal fabrykaStrzal;

    @WithFactory
    public Statek(Bento bento) {
        // tu powinno się dać wstrzyknąć FabrykaStrzal, ale najwyraźniej funkcjonalność jeszcze nie działa :)
        // bo "FabrykaStrzal" jako interfejs tworzy implementację FabrykaStrzalImpl, więc
        // fabryka nazywa się FabrykaStrzalImplFactory, a StatekFactory szuka po nazwie interfejsu FabrykaStrzalFactory,
        // i jest compile-time error.
        this.fabrykaStrzal = bento.get(FabrykaStrzalImplFactory.IT);
    }

    public void strzelaj() {
        List<Strzala> strzaly = new ArrayList<>();
        for (String kolor : Arrays.asList("czerwona", "zielona", "niebieska")) {
            strzaly.add(fabrykaStrzal.wyprodukuj(kolor));
        }
        strzaly.add(fabrykaStrzal.wyprodukuj("złota", 1000));

        for (Strzala strzala : strzaly) {
            System.out.println(strzala);
        }
    }


    public static void main(String[] args) {
        // statek nie ma zależności od Bento, ale potrafi robić ładnie skonfigurowane, dynamiczne obiekty

        Bento root = Bento.createRoot();
        root.register("szybkosc", "0.1f"); // domyślna szybkość strzały

        root.get(StatekFactory.IT).strzelaj();

    }

}
