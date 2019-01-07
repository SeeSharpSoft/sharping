package net.seesharpsoft.spring.multipart.boot.demo.util;

import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.DataHelper;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;
import org.hibernate.usertype.ParameterizedType;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ElementCollectionFun extends AbstractSingleColumnStandardBasicType<List> implements ParameterizedType {

    private static class ElementCollection extends AbstractTypeDescriptor<List> {

        public static final ElementCollection INSTANCE = new ElementCollection();

        private Class elementType;

        public ElementCollection() {
            super(List.class);
        }

        public ElementCollection(Class elementType) {
            this();
            this.elementType = elementType;
        }

        @Override
        public String toString(List list) {
            if (list == null) {
                return "<>";
            }
            return "<"
                    + String.join(">|<", (List<String>)list.stream().map(element -> element == null ? "" : element.toString()).collect(Collectors.toList()))
                    + ">";
        }

        @Override
        public List fromString(String value) {
            if (value == null || value.length() < 2) {
                return new ArrayList();
            }
            value = value.substring(1, value.length() - 1);
            List<String> values = Arrays.asList(value.split(">|<"));
            return elementType == null ? values
                    : values.stream().map(valueString -> {
                try {
                    Constructor constructor = elementType.getConstructor(String.class);
                    return constructor.newInstance(valueString);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException exc) {
                    exc.printStackTrace();
                    throw new UnsupportedOperationException("constructor accepting single String argument must be provided");
                }
            }).collect(Collectors.toList());
        }

        @Override
        public <X> X unwrap(List list, Class<X> type, WrapperOptions options) {
            if (list == null) {
                return null;
            }
            String value = toString(list);
            if (String.class.isAssignableFrom(type)) {
                return (X)value;
            } else if (Reader.class.isAssignableFrom(type)) {
                return (X)new StringReader(value);
            } else if (CharacterStream.class.isAssignableFrom(type)) {
                return (X)new CharacterStreamImpl(value);
            } else if (Clob.class.isAssignableFrom(type)) {
                return (X)options.getLobCreator().createClob(value);
            } else if (DataHelper.isNClob(type)) {
                return (X)options.getLobCreator().createNClob(value);
            } else {
                throw this.unknownUnwrap(type);
            }
        }

        @Override
        public <X> List wrap(X value, WrapperOptions options) {
            String valueString = null;
            if (value == null) {
                return new ArrayList();
            } else if (String.class.isInstance(value)) {
                valueString = (String) value;
            } else if (Reader.class.isInstance(value)) {
                valueString = DataHelper.extractString((Reader) value);
            } else if (Clob.class.isInstance(value)) {
                valueString = DataHelper.extractString((Clob) value);
            } else {
                throw this.unknownWrap(value.getClass());
            }
            return fromString(valueString);
        }
    }

    public ElementCollectionFun(SqlTypeDescriptor sqlTypeDescriptor, JavaTypeDescriptor javaTypeDescriptor) {
        super(VarcharTypeDescriptor.INSTANCE, ElementCollection.INSTANCE);
    }

    @Override
    public void setParameterValues(Properties properties) {
        if (properties.containsKey("type")) {
            try {
                setJavaTypeDescriptor(new ElementCollection(Class.forName(properties.getProperty("type"))));
            } catch (ClassNotFoundException exc) {
                exc.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return "ElementCollectionFun";
    }
}
