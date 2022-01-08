package com.nihalsoft.java.jdbc.orm.result.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.support.JdbcUtils;

import com.nihalsoft.java.jdbc.orm.common.DataMap;

public class DataMapMapper implements RowHandler<DataMap> {

    public DataMap mapRow(ResultSet rs, String[] columnNames, int rowNum) throws SQLException {
        int columnCount = columnNames.length;
        DataMap res = DataMap.create();
        for (int i = 1; i <= columnCount; i++) {
            res.put(columnNames[i-1], JdbcUtils.getResultSetValue(rs, i));
        }
        return res;
    }

}
