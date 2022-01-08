package com.nihalsoft.java.jdbc.orm.result.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface RowHandler<T> {

    @Nullable
    T mapRow(ResultSet rs, String[] columnNames, int rowNum) throws SQLException;

}