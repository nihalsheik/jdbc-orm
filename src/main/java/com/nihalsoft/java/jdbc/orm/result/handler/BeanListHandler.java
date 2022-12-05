package com.nihalsoft.java.jdbc.orm.result.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class BeanListHandler<T> implements ResultSetExtractor<List<T>> {

    private BeanProcessor beanProcessor;
    private Class<T> type;

    public BeanListHandler(BeanProcessor beanProcessor, Class<T> type) {
        this.beanProcessor = beanProcessor;
        this.type = type;
    }

    @Override
    public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
        return beanProcessor.toBeanList(rs, type);
    }

    
    
}
