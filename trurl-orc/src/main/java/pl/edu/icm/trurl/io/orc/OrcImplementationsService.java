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

package pl.edu.icm.trurl.io.orc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.orc.OrcConf;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;

import java.io.IOException;

/**
 * This service wraps static calls to Orc factory methods
 * and helps to centralize configuration.
 */
public class OrcImplementationsService {

    private final Configuration configuration = new Configuration();

    public OrcImplementationsService() {
        OrcConf.OVERWRITE_OUTPUT_FILE.setBoolean(configuration, true);
    }

    public Writer createWriter(String pathString, TypeDescription typeDescription) throws IOException {
        return OrcFile
                .createWriter(new Path(pathString), OrcFile.writerOptions(configuration)
                        .setSchema(typeDescription));
    }

    public Reader createReader(String pathString) throws IOException {
        return OrcFile.createReader(new Path(pathString),
                OrcFile.readerOptions(configuration));
    }
}
