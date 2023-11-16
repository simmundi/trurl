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
import net.snowyhollows.bento.category.CategoryManager;

public class Pieces extends CategoryManager<Piece> {

    public final Piece QUEEN;
    public final Piece PAWN;

    @WithFactory
    public Pieces(Bento bento) {
        super(bento, "chess.piece", PieceFactory.IT);
        QUEEN = getByName("QUEEN");
        PAWN = getByName("PAWN");
    }

    @Override
    public Piece[] emptyArray() {
        return new Piece[0];
    }
}
