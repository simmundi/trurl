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

package pl.edu.icm.trurl.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import net.snowyhollows.bento.soft.SoftEnumManager;
import pl.edu.icm.trurl.ecs.entity.IdentityMapSession;
import pl.edu.icm.trurl.ecs.SessionEntity;
import pl.edu.icm.trurl.ecs.mapper.LifecycleEvent;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreConfigurer;
import pl.edu.icm.trurl.store.attribute.*;
import pl.edu.icm.trurl.store.join.ArrayJoin;
import pl.edu.icm.trurl.store.join.RangedJoin;
import pl.edu.icm.trurl.store.join.SingleJoin;
import pl.edu.icm.trurl.store.reference.ArrayReference;
import pl.edu.icm.trurl.store.reference.SingleReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class CommonTypes {
    public static final ClassName COMPONENT_STORE_METADATA = ClassName.get(StoreConfigurer.class);
    public static final ClassName STORE = ClassName.get(Store.class);
    public static final ClassName LIFECYCLE_EVENT = ClassName.get(LifecycleEvent.class);
    public static final ClassName INT_SINK = ClassName.get(IntSink.class);
    public static final ClassName MAPPER = ClassName.get(Mapper.class);
    public static final ClassName MAPPERS = ClassName.get(Mappers.class);
    public static final ClassName ARRAY_JOIN = ClassName.get(ArrayJoin.class);
    public static final ClassName RANGED_JOIN = ClassName.get(RangedJoin.class);
    public static final ClassName SINGLE_JOIN = ClassName.get(SingleJoin.class);
    public static final ClassName ARRAY_REFERENCE = ClassName.get(ArrayReference.class);
    public static final ClassName SINGLE_REFERENCE = ClassName.get(SingleReference.class);
    public static final ClassName SESSION = ClassName.get(IdentityMapSession.class);
    public static final ClassName LIST = ClassName.get(List.class);
    public static final ClassName LANG_STRING = ClassName.get(String.class);
    public static final ClassName ARRAYS = ClassName.get(Arrays.class);
    public static final ClassName COLLECTIONS = ClassName.get(Collections.class);
    public static final ParameterizedTypeName MAPPER_LIST = ParameterizedTypeName.get(LIST, MAPPER);
    public static final ParameterizedTypeName ENTITY_LIST = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(SessionEntity.class));
    public static final ParameterizedTypeName ATTRIBUTE_LIST = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Attribute.class));
    public static final ParameterizedTypeName ATTRIBUTE_ARRAY_LIST = ParameterizedTypeName.get(ClassName.get(ArrayList.class), ClassName.get(Attribute.class));
    public static final ClassName ENTITY = ClassName.get(SessionEntity.class);
    public static final ClassName ATOMIC_INTEGER_ARRAY = ClassName.get(AtomicIntegerArray.class);
    public static final ClassName INT_COLUMN = ClassName.get(IntAttribute.class);
    public static final ClassName DOUBLE_COLUMN = ClassName.get(DoubleAttribute.class);
    public static final ClassName FLOAT_COLUMN = ClassName.get(FloatAttribute.class);
    public static final ClassName BYTE_COLUMN = ClassName.get(ByteAttribute.class);
    public static final ClassName SHORT_COLUMN = ClassName.get(ShortAttribute.class);
    public static final ClassName STRING_COLUMN = ClassName.get(StringAttribute.class);
    public static final ClassName CATEGORICAL_STATIC_COLUMN = ClassName.get(CategoricalStaticAttribute.class);
    public static final ClassName BOOLEAN_COLUMN = ClassName.get(BooleanAttribute.class);
    public static final ClassName CATEGORICAL_DYNAMIC_COLUMN = ClassName.get(CategoricalDynamicAttribute.class);
    public static final ClassName SOFT_ENUM_MANAGER = ClassName.get(SoftEnumManager.class);


    private CommonTypes() {
    }
}
