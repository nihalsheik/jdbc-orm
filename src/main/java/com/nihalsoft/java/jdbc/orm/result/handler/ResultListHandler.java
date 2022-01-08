package com.nihalsoft.java.jdbc.orm.result.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

public class ResultListHandler<T> implements ResultSetExtractor<List<T>> {

    private final RowHandler<T> rowMapper;
    private ResultSetMetaData metaData;

    public ResultListHandler(RowHandler<T> rowMapper) {
        Assert.notNull(rowMapper, "RowMapper is required");
        this.rowMapper = rowMapper;
    }

    public ResultSetMetaData getMetaData() {
        return metaData;
    }

    @Override
    public List<T> extractData(ResultSet rs) throws SQLException {

        this.metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = JdbcUtils.lookupColumnName(metaData, i);
        }

        List<T> results = new ArrayList<T>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(this.rowMapper.mapRow(rs, columnNames, rowNum++));
        }
        return results;
    }

}
