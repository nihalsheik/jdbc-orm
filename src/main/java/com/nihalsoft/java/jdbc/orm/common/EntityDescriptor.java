package com.nihalsoft.java.jdbc.orm.common;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

import com.nihalsoft.java.jdbc.orm.annotation.Table;

//TODO change name to TableInfo
public class EntityDescriptor {

    private Table table;

    private Map<String, ColumnInfo> columns;

    private DataMap dataMap;

    public EntityDescriptor(Table table, Map<String, ColumnInfo> columns, DataMap dataMap) {
        this.table = table;
        this.columns = columns;
        this.dataMap = dataMap;
    }

    public Table getTable() {
        return this.table;
    }

    public String getTableName() {
        return this.table.name();
    }

    public Map<String, ColumnInfo> getColumns() {
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

    public Object getValue(String name) {
        return this.columns.get(name).getValue();
    }

    public ColumnInfo getColumnInfo(String name) {
        return this.columns.get(name);
    }

    public String getSqlForInsert(String criteria) {
        return this._getSql("INSERT INTO", criteria);
    }

    public String getSqlForUpdate(String criteria) {
        return this._getSql("UPDATE", criteria);
    }

    public String getSqlForUpdate(String criteria, String[] cols) {
        return this._getSql("UPDATE", criteria);
    }

    public String getSqlForDelete(String criteria) {
        return "DELETE FROM " + this.getTableName() + " " + criteria;
    }

    public String _getSql(String prefix, String criteria) {

        var sBuilder = new StringBuilder();
        iterateColumn(ci -> sBuilder.append(",").append(ci.getName()).append("=?"));

        String sql = prefix + " " + this.getTableName() + " SET " + sBuilder.substring(1);

        if (!criteria.equals("")) {
            sql += " WHERE " + criteria;
        }

        return sql;
    }

    public void iterateColumn(Consumer<ColumnInfo> consumer) {
        this.columns.forEach((k, v) -> consumer.accept(v));
    }

}
