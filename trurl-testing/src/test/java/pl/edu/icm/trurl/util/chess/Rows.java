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

package pl.edu.icm.trurl.util.chess;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnumManager;

public class Rows extends SoftEnumManager<Row> {
    public final Row _1;
    public final Row _2;
    public final Row _3;
    public final Row _4;
    public final Row _5;
    public final Row _6;
    public final Row _7;
    public final Row _8;

    @WithFactory
    public Rows(Bento bento) {
        super(bento, "chess.row", RowFactory.IT);
        _1 = getByName("1");
        _2 = getByName("2");
        _3 = getByName("3");
        _4 = getByName("4");
        _5 = getByName("5");
        _6 = getByName("6");
        _7 = getByName("7");
        _8 = getByName("8");
    }

    @Override
    public Row[] emptyArray() {
        return new Row[0];
    }
}
