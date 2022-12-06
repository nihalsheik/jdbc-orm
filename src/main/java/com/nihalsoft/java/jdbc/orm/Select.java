package com.nihalsoft.java.jdbc.orm;

import java.util.List;

import com.nihalsoft.java.jdbc.orm.common.DataMap;
import com.nihalsoft.java.jdbc.orm.common.EntityUtil;

public class Select {

    private String tableName = "";
    private String where = "";
    private Object[] args;
    private String orderBy;
    private DataMap data;
    private Class<?> entity;

    private Jdbc jdbc;

    public Select(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public Select(Jdbc jdbc, Class<?> entity) {
        this.jdbc = jdbc;
        this.entity = entity;
        this.tableName = EntityUtil.getTableName(entity);
    }

    public Select(Jdbc jdbc, String tableName) {
        this.jdbc = jdbc;
        this.tableName = tableName;
    }

    public Select table(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public Select data(DataMap data) {
        this.data = data;
        return this;
    }

    public Select where(String where) {
        this.where = where;
        return this;
    }

    public Select args(Object... args) {
        this.args = args;
        return this;
    }

    public Select orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public <T> List<T> findAll(Class<T> classType) throws Exception {
        String sql = "SELECT * FROM " + tableName;
        return jdbc.queryForBeanList(sql, classType, args);
    }

    public void update() throws Exception {
        jdbc.update(tableName, data, where, args);
    }

}