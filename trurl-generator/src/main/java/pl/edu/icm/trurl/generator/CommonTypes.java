package pl.edu.icm.trurl.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.MapperListeners;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.PrefixedStore;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreMetadata;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.BooleanAttribute;
import pl.edu.icm.trurl.store.attribute.ByteAttribute;
import pl.edu.icm.trurl.store.attribute.DoubleAttribute;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;
import pl.edu.icm.trurl.store.attribute.EntityListAttribute;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;
import pl.edu.icm.trurl.store.attribute.FloatAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.ShortAttribute;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommonTypes {
    public static final ClassName COMPONENT_STORE_METADATA = ClassName.get(StoreMetadata.class);
    public static final ClassName STORE = ClassName.get(Store.class);
    public static final ClassName PREFIXED_STORE = ClassName.get(PrefixedStore.class);
    public static final ClassName MAPPER = ClassName.get(Mapper.class);
    public static final ClassName MAPPERS = ClassName.get(Mappers.class);
    public static final ClassName MAPPER_LISTENERS = ClassName.get(MapperListeners.class);
    public static final ClassName SESSION = ClassName.get(Session.class);
    public static final ClassName LIST = ClassName.get(List.class);
    public static final ClassName LANG_OBJECT = ClassName.get(Object.class);
    public static final ClassName LANG_OBJECTS = ClassName.get(Objects.class);
    public static final ClassName LANG_STRING = ClassName.get(String.class);
    public static final ClassName ARRAYS = ClassName.get(Arrays.class);
    public static final ParameterizedTypeName ENTITY_LIST = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Entity.class));
    public static final ParameterizedTypeName ATTRIBUTE_LIST = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Attribute.class));
    public static final ParameterizedTypeName ATTRIBUTE_ARRAY_LIST = ParameterizedTypeName.get(ClassName.get(ArrayList.class), ClassName.get(Attribute.class));
    public static final ClassName ENTITY = ClassName.get(Entity.class);

    public static final ClassName INT_COLUMN = ClassName.get(IntAttribute.class);
    public static final ClassName DOUBLE_COLUMN = ClassName.get(DoubleAttribute.class);
    public static final ClassName FLOAT_COLUMN = ClassName.get(FloatAttribute.class);
    public static final ClassName BYTE_COLUMN = ClassName.get(ByteAttribute.class);
    public static final ClassName SHORT_COLUMN = ClassName.get(ShortAttribute.class);
    public static final ClassName STRING_COLUMN = ClassName.get(StringAttribute.class);
    public static final ClassName ENUM_COLUMN = ClassName.get(EnumAttribute.class);
    public static final ClassName BOOLEAN_COLUMN = ClassName.get(BooleanAttribute.class);
    public static final ClassName ENTITY_LIST_COLUMN = ClassName.get(EntityListAttribute.class);
    public static final ClassName ENTITY_COLUMN = ClassName.get(EntityAttribute.class);

    private CommonTypes() {
    }
}