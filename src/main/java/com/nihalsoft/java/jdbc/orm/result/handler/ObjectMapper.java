package com.nihalsoft.java.jdbc.orm.result.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.support.JdbcUtils;

public class ObjectMapper implements RowHandler<Object[]> {

    public Object[] mapRow(ResultSet rs, String[] columnNames, int rowNum) throws SQLException {
        int columnCount = columnNames.length;
        Object[] obj = new Object[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            obj[i - 1] = JdbcUtils.getResultSetValue(rs, i);
        }
        return obj;
    }

}
