package com.nihalsoft.java.jdbc.orm.result.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

public class ResultHandler<T> implements ResultSetExtractor<T> {

    private final RowHandler<T> rowMapper;
    private ResultSetMetaData metaData;

    public ResultHandler(RowHandler<T> rowMapper) {
        Assert.notNull(rowMapper, "RowMapper is required");
        this.rowMapper = rowMapper;
    }

    public ResultSetMetaData getMetaData() {
        return metaData;
    }

    @Override
    public T extractData(ResultSet rs) throws SQLException {

        if (!rs.next()) {
            return null;
        }

        this.metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = JdbcUtils.lookupColumnName(metaData, i);
        }

        T result = this.rowMapper.mapRow(rs, columnNames, 0);

        return result;
    }

}
