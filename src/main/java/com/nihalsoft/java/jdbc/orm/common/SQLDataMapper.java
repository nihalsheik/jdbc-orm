package com.nihalsoft.java.jdbc.orm.common;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

public class SQLDataMapper implements RowMapper<DataMap> {

    public DataMap mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        DataMap res = DataMap.create();
        for (int i = 1; i <= columnCount; i++) {
            String column = JdbcUtils.lookupColumnName(rsmd, i);
            res.put(column, JdbcUtils.getResultSetValue(rs, i));
        }
        return res;
    }

}
