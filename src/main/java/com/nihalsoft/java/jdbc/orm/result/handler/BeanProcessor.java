package com.nihalsoft.java.jdbc.orm.result.handler;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nihalsoft.java.jdbc.orm.annotation.Column;

public class BeanProcessor {

    private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<Class<?>, Object>();

    static {
        primitiveDefaults.put(Integer.TYPE, Integer.valueOf(0));
        primitiveDefaults.put(Short.TYPE, Short.valueOf((short) 0));
        primitiveDefaults.put(Byte.TYPE, Byte.valueOf((byte) 0));
        primitiveDefaults.put(Float.TYPE, Float.valueOf(0f));
        primitiveDefaults.put(Double.TYPE, Double.valueOf(0d));
        primitiveDefaults.put(Long.TYPE, Long.valueOf(0L));
        primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
        primitiveDefaults.put(Character.TYPE, Character.valueOf((char) 0));
    }

    public BeanProcessor() {
    }

    public <T> List<T> toBeanList(ResultSet rs, Class<? extends T> type) throws SQLException {

        List<T> list = new ArrayList<T>();

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
//            ExtendedBeanInfoFactory bi = new ExtendedBeanInfoFactory();
//            BeanInfo beanInfo = bi.getBeanInfo(type);
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            int[] columnToProperty = mapColumnsToProperties(rs, props);
            while (rs.next()) {
                list.add(populateBean(rs, type, props, columnToProperty));
            }

        } catch (IntrospectionException e) {
            throw new SQLException("Bean introspection failed: " + e.getMessage());
        }
        return list;
    }

    public <T> T toBean(ResultSet rs, Class<? extends T> type) throws SQLException {

        try {
            if (!rs.next()) {
                return null;
            }
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            int[] columnToProperty = mapColumnsToProperties(rs, props);
            return populateBean(rs, type, props, columnToProperty);

        } catch (Exception e) {
            throw new SQLException("Bean introspection failed: " + e.getMessage());
        }
    }

    private <T> T populateBean(ResultSet rs, Class<? extends T> type, PropertyDescriptor[] props,
            int[] columnToProperty) throws SQLException {

        T bean;

        try {
            bean = type.newInstance();
        } catch (Exception e) {
            throw new SQLException("Cannot create " + type.getName() + ": " + e.getMessage());

        }

        Method setter = null;

        for (int i = 0; i < props.length; i++) {

            if (columnToProperty[i] == -1) {
                continue;
            }

            PropertyDescriptor prop = props[i];
            Class<?> propType = prop.getPropertyType();

            Object value = null;
            if (propType != null) {
                value = rs.getObject(columnToProperty[i]);
                if (value == null && propType.isPrimitive()) {
                    value = primitiveDefaults.get(propType);
                }
            }

            setter = prop.getWriteMethod();

            if (setter == null || setter.getParameterTypes().length != 1) {
                continue;
            }

            try {
                Class<?> firstParam = setter.getParameterTypes()[0];

                if (value instanceof LocalDateTime) {
                    LocalDateTime dTime = (LocalDateTime) value;
                    value = java.util.Date.from(dTime.atZone(ZoneId.systemDefault()).toInstant());
                }

               // if (value == null || firstParam.isInstance(value) || matchesPrimitive(firstParam, value.getClass())) {
                    setter.invoke(bean, new Object[] { value });
               // } else {
               //     throw new SQLException("Cannot set " + prop.getName() + ": incompatible types, cannot convert "
               //             + value.getClass().getName() + " to " + firstParam.getName());
//
               //  }

            } catch (Exception e) {
                throw new SQLException("Cannot set " + prop.getName() + ": " + e.getMessage());
            }
        }

        return bean;
    }

    private boolean matchesPrimitive(Class<?> targetType, Class<?> valueType) {
        if (!targetType.isPrimitive()) {
            return false;
        }

        try {

            Field typeField = valueType.getField("TYPE");
            Object primitiveValueType = typeField.get(valueType);

            if (targetType == primitiveValueType) {
                return true;
            }
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }
        return false;
    }

    protected int[] mapColumnsToProperties(ResultSet rs, PropertyDescriptor[] props) throws SQLException {

        int plen = props.length;
        int[] t = new int[plen];
        for (int i = 0; i < plen; i++) {
            Method getter = props[i].getReadMethod();
            Column e = getter.getAnnotation(Column.class);
            if (e == null) {
                t[i] = -1;
                continue;
            }
            String columnName = !e.name().equals("") ? e.name() : props[i].getName();
            try {
                t[i] = rs.findColumn(columnName);
            } catch (Exception ex) {
                t[i] = -1;
            }

        }

        return t;
    }

}
