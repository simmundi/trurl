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

package pl.edu.icm.trurl.io.parser;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {

    @Test
    void nextSimpleValue() {
        Parser parser = parser("alamakota,123,test,3.14");
        String result = parser.nextSimpleValue();
        assertThat(result).isEqualTo("alamakota");
    }

    @Test
    void nextSimpleValue__nextLine() {
        Parser parser = parser("alamakota\rto,123,test,3.14");
        String result = parser.nextSimpleValue();
        assertThat(result).isEqualTo("alamakota");
    }

    @Test
    void nextSimpleValue__quotes() {
        Parser parser = parser("ala ma \"kota\",123,test,3.14");
        String result = parser.nextSimpleValue();
        assertThat(result).isEqualTo("ala ma \"kota\"");
    }

    @Test
    void nextQuotedValue() {
        Parser parser = parser("\"alamakota\",123,test,3.14");
        String result = parser.nextQuotedValue();
        assertThat(result).isEqualTo("alamakota");
    }

    @Test
    void nextQuotedValue__nextLine() {
        Parser parser = parser("\"ala\rma\nkota\",123,test,3.14");
        String result = parser.nextQuotedValue();
        assertThat(result).isEqualTo("ala\rma\nkota");
    }

    @Test
    void nextQuotedValue__quotes() {
        Parser parser = parser("\"ala ma \"\"kota\"\"\",123,test,3.14");
        String result = parser.nextQuotedValue();
        assertThat(result).isEqualTo("ala ma \"kota\"");
    }

    @Test
    void nextValue__quoted() {
        Parser parser = parser("\"Kazimierz\",123,test,3.14");
        String result = parser.nextValue();
        assertThat(result).isEqualTo("Kazimierz");
    }

    @Test
    void nextValue__unquoted() {
        Parser parser = parser("Kazimierz,123,test,3.14");
        String result = parser.nextValue();
        assertThat(result).isEqualTo("Kazimierz");
    }

    @Test
    void nextCsvRow__other() {
        Parser parser = parser("Jan,1,45,A,\"1,2,3\",,true\nFilip,2,45,B,\"7,8\",2,false");
        List result = new ArrayList();
        List result2 = new ArrayList();
        parser.nextCsvRow(result);
        parser.nextLine();
        parser.nextCsvRow(result2);

        assertThat(result).containsExactly("Jan", "1", "45", "A", "1,2,3", "", "true");
        assertThat(result2).containsExactly("Filip", "2", "45", "B", "7,8", "2", "false");
        assertThat(parser.nextIsEof()).isTrue();

    }

    @Test
    void nextCsvRow__empty_end() {
        Parser parser = parser("Adam,3,45,C,,,");
        List result = new ArrayList();
        parser.nextCsvRow(result);

        assertThat(result).hasSize(7);
    }

    @Test
    void nextCsvRow() {
        Parser parser = parser("Kazimierz,123,test,3.14\na,b,c,d");
        List result = new ArrayList();
        List result2 = new ArrayList();
        parser.nextCsvRow(result);
        parser.nextLine();
        parser.nextCsvRow(result2);

        assertThat(result).containsExactly("Kazimierz", "123", "test", "3.14");
        assertThat(result2).containsExactly("a", "b", "c", "d");
    }

    @Test
    void nextPropertyRow() {
        Parser parser = parser("# some comment    \r\nname=Filip     # asd asd asd asd \nlast.name=Kokoszka");
        Map<String, String> result = new HashMap<>();
        parser.nextPropertiesLine(result); // comment
        parser.nextLine();
        parser.nextPropertiesLine(result); // name
        parser.nextLine();
        parser.nextPropertiesLine(result); // last.name

        assertThat(result).containsEntry("name", "Filip");
        assertThat(result).containsEntry("last.name", "Kokoszka");
        assertThat(result).hasSize(2);
        assertThat(parser.nextIsEof()).isTrue();
    }

    private Parser parser(String string) {
        Parser parser = new Parser(new StringReader(string));
        return parser;
    }
}
