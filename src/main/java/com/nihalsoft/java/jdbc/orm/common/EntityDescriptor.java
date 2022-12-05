package com.nihalsoft.java.jdbc.orm.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//TODO change name to TableInfo
public class EntityDescriptor {

    private String tableName = "";

    private List<ColumnInfo> columns;

    private DataMap dataMap;

    public EntityDescriptor(String tableName, List<ColumnInfo> columns, DataMap dataMap) {
        this.tableName = tableName;
        this.columns = columns;
        this.dataMap = dataMap;
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public DataMap toDataMap() {
        return dataMap;
    }

    public Object[] getValues(Object... params) {

        var values = new ArrayList<Object>();

        iterateColumn(ci -> values.add(ci.getValue()));

        for (Object obj : params) {
            values.add(obj);
        }

        return values.toArray();
    }

    public String getSqlStringForInsert(String criteria) {
        return this._getSqlString("INSERT INTO", criteria);
    }

    public String getSqlStringForUpdate(String criteria) {
        return this._getSqlString("UPDATE", criteria);
    }

    public String getSqlStringForUpdate(String criteria, String[] cols) {
        return this._getSqlString("UPDATE", criteria);
    }

    public String getSqlStringForDelete(String criteria) {
        return "DELETE FROM " + this.getTableName() + " " + criteria;
    }

    public String _getSqlString(String prefix, String criteria) {

        var sBuilder = new StringBuilder();
        iterateColumn(ci -> sBuilder.append(",").append(ci.getName()).append("=?"));

        String sql = prefix + " " + this.getTableName() + " SET " + sBuilder.substring(1);

        if (!criteria.equals("")) {
            sql += " WHERE " + criteria;
        }

        return sql;
    }

    public void iterateColumn(Consumer<ColumnInfo> consumer) {
        for (ColumnInfo ci : this.getColumns()) {
            consumer.accept(ci);
        }
    }

}
