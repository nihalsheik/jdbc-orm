package com.nihalsoft.java.jdbc.orm.result.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BeanMapper<T> implements RowHandler<T> {


    private BeanProcessor beanProcessor;
    private Class<T> type;

    public BeanMapper(BeanProcessor beanProcessor, Class<T> type) {
        this.beanProcessor = beanProcessor;
        this.type = type;
    }

    public T mapRow(ResultSet rs, String[] columnNames, int rowNum) throws SQLException {
        return this.beanProcessor.toBean(rs, type);
    }

}
