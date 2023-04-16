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

package pl.edu.icm.trurl.util;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.Configurer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.util.chess.*;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class SoftEnumCubeTest {
    @Test
    @DisplayName("Should create a cube with the proper size")
    void construct() throws IOException {
        // given
        Bento bento = new Configurer().loadConfigResource("/chess.properties").getConfig();

        // execute
        SoftEnumCube<Row, Column, Board, Piece> chessBoard = new SoftEnumCube<>(
                bento.get(RowsFactory.IT),
                bento.get(ColumnsFactory.IT),
                bento.get(BoardsFactory.IT)
        );

        // assert
        assertThat(chessBoard.stream()).hasSize(0);
        assertThat(chessBoard.getRowCount()).isEqualTo(8);
        assertThat(chessBoard.getColumnCount()).isEqualTo(8);
        assertThat(chessBoard.getLayerCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should store and retrieve data")
    void put_get_stream() throws IOException {
        // given
        Bento bento = new Configurer().loadConfigResource("/chess.properties").getConfig();
        Rows rows = bento.get(RowsFactory.IT);
        Columns columns = bento.get(ColumnsFactory.IT);
        Boards boards = bento.get(BoardsFactory.IT);
        Pieces pieces = bento.get(PiecesFactory.IT);
        SoftEnumCube<Column, Row, Board, Piece> chessBoard = new SoftEnumCube<>(columns, rows, boards);

        // execute
        chessBoard.put(columns.D, rows._3, boards.FIRST, pieces.PAWN);
        chessBoard.put(columns.F, rows._7, boards.FIRST, pieces.PAWN);
        chessBoard.put(columns.A, rows._8, boards.FIRST, pieces.PAWN);
        chessBoard.put(columns.A, rows._8, boards.FIRST, pieces.QUEEN);
        chessBoard.put(columns.A, rows._8, boards.THIRD, pieces.PAWN);

        Row _3 = rows.getByName("3");
        Row _7 = rows.getByName("7");
        Row _8 = rows.getByName("8");

        Board first = boards.getByName("FIRST");
        Board third = boards.getByName("THIRD");

        Column d = columns.getByOrdinal(3); // 0 based
        Column f = columns.getByOrdinal(5);
        Column a = columns.getByOrdinal(0);

        // assert
        assertThat(chessBoard.stream()).containsExactlyInAnyOrder(
                Quadruple.of(d, _3, first, pieces.PAWN),
                Quadruple.of(f, _7, first, pieces.PAWN),
                Quadruple.of(a, _8, first, pieces.QUEEN),
                Quadruple.of(a, _8, third, pieces.PAWN));

        assertThat(chessBoard.get(d, _7, first)).isNull();
        assertThat(chessBoard.get(d, _8, first)).isNull();
        assertThat(chessBoard.get(f, _3, first)).isNull();
        assertThat(chessBoard.get(f, _8, first)).isNull();
        assertThat(chessBoard.get(d, _3, first)).isSameAs(pieces.PAWN);
        assertThat(chessBoard.get(f, _7, first)).isSameAs(pieces.PAWN);
        assertThat(chessBoard.get(a, _8, first)).isSameAs(pieces.QUEEN);
        assertThat(chessBoard.get(a, _8, third)).isSameAs(pieces.PAWN);
    }
}

