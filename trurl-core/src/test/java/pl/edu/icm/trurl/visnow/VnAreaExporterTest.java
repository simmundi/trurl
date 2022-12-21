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

package pl.edu.icm.trurl.visnow;

import net.snowyhollows.bento.config.WorkDir;
import org.assertj.core.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VnAreaExporterTest {

    @Mock
    WorkDir workDir;

    @Spy
    Mapper<AreaInfo> areaInfoMapper = Mappers.create(AreaInfo.class);

    VnAreaExporter<AreaInfo> vnAreaExporter;

    @BeforeEach
    void before() throws FileNotFoundException {
        vnAreaExporter = VnAreaExporter.create(
                workDir,
                AreaInfo.class,
                "mushrooms",
                0, 10, 10, 10);
    }

    @Test
    @DisplayName("Should save grid data to correct places in the store")
    void append() throws IOException {
        // given
        vnAreaExporter = VnAreaExporter.create(
                workDir,
                areaInfoMapper,
                "trees",
                10, 10, 1000, 20);

        // execute
        AreaInfo areaA = new AreaInfo(5);
        AreaInfo areaB = new AreaInfo(6);
        AreaInfo areaC = new AreaInfo(7);

        vnAreaExporter.append(10, 1000, areaA);
        vnAreaExporter.append(19, 1019, areaB);
        vnAreaExporter.append(14, 1010, areaC);

        // assert
        verify(areaInfoMapper).configureStore(notNull());
        verify(areaInfoMapper).attachStore(notNull());
        verify(areaInfoMapper, times(2)).ensureCapacity(200);
        verify(areaInfoMapper).save(any(), same(areaA), eq(0));
        verify(areaInfoMapper).save(any(), same(areaB), eq(199));
        verify(areaInfoMapper).save(any(), same(areaC), eq(104));
    }

    @Test
    void close() throws IOException {
        // given
        ByteArrayOutputStream vnd = new ByteArrayOutputStream();
        ByteArrayOutputStream vnf = new ByteArrayOutputStream();
        Mockito.when(workDir.openForWriting(new File("trees.vnd"))).thenReturn(vnd);
        Mockito.when(workDir.openForWriting(new File("trees.vnf"))).thenReturn(vnf);
        vnAreaExporter = VnAreaExporter.create(
                workDir,
                areaInfoMapper,
                "trees",
                10, 10, 1000, 20);

        // execute
        vnAreaExporter.close();

        // assert
        Assertions.assertThat(vnd.size()).isEqualTo(800);
        Assertions.assertThat(Arrays.asList(vnf.toString().split("\\R"))).containsExactly(
                "#VisNow regular field",
                "field \"trees\", dims 10 20",
                "x 10 20",
                "y 1000 1020",
                "component countOfTrees int",
                "file \"trees.vnd\" binary",
                "countOfTrees"
        );
    }
}
