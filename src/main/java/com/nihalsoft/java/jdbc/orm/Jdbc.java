package com.nihalsoft.java.jdbc.orm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.nihalsoft.java.jdbc.orm.common.ColumnInfo;
import com.nihalsoft.java.jdbc.orm.common.EntityDescriptor;
import com.nihalsoft.java.jdbc.orm.common.EntityUtil;
import com.nihalsoft.java.jdbc.orm.common.Util;
import com.nihalsoft.java.jdbc.orm.result.handler.BeanListHandler;
import com.nihalsoft.java.jdbc.orm.result.handler.BeanMapper;
import com.nihalsoft.java.jdbc.orm.result.handler.BeanProcessor;
import com.nihalsoft.java.jdbc.orm.result.handler.ObjectMapper;
import com.nihalsoft.java.jdbc.orm.result.handler.ResultHandler;
import com.nihalsoft.java.jdbc.orm.result.handler.ResultListHandler;

public class Jdbc extends JdbcTemplate {

    private BeanProcessor beanProcessor;

    private Logger log = Logger.getLogger("Jdbc");

    public Jdbc(DataSource ds) {
        super(ds);
        beanProcessor = new BeanProcessor();
    }

    public List<Object[]> queryForObjectList(String sql, Object... args) throws Exception {
        return this.query(sql, new ResultListHandler<Object[]>(new ObjectMapper()));
    }

    public Object[] queryForObject(String sql, Object... args) throws Exception {
        return this.query(sql, new ResultHandler<Object[]>(new ObjectMapper()));
    }

    public <T> List<T> queryForBeanList(String sql, Class<T> type, Object... args) throws Exception {
        return this.query(sql, new BeanListHandler<T>(beanProcessor, type));
    }

    public <T> T queryForBean(String sql, Class<T> type, Object... args) throws Exception {
        return this.query(sql, new ResultHandler<T>(new BeanMapper<T>(beanProcessor, type)));
    }

    public int update(String tableName, Map<String, Object> dataMap, String criteria, Object... params)
            throws Exception {

        Object[] values = new Object[dataMap.size() + params.length];

        String col = "";
        int i = 0;
        for (Entry<String, Object> entry : dataMap.entrySet()) {
            col += "," + entry.getKey() + "=?";
            values[i++] = entry.getValue();
        }

        for (Object obj : params) {
            values[i++] = obj;
        }

        String sql = "UPDATE " + tableName + " SET " + col.substring(1) + " WHERE " + criteria;
        return this.update(sql, values);
    }

    public <T> T findOne(Class<T> clazz, Object id) throws Exception {
        EntityDescriptor ed = EntityUtil.getDescriptor(clazz);
        Util.throwSqlException(!ed.hasId(), "There is no id column");
        return this.queryForBean("SELECT * FROM " + ed.getTableName() + " WHERE " + ed.getIdColumn() + "=?", clazz, id);
    }

    /**
     * 
     * @param <T>
     * @param clazz
     * @return
     * @throws Exception
     */
    public <T> List<T> findAll(Class<T> clazz) throws Exception {
        return this.queryForBeanList("SELECT * FROM " + EntityUtil.getTableName(clazz), clazz);
    }

    /**
     * 
     * @param <T>
     * @param clazz
     * @param criteria
     * @param args
     * @return
     * @throws Exception
     */
    public <T> List<T> find(Class<T> clazz, String criteria, Object... args) throws Exception {
        return this.queryForBeanList("SELECT * FROM " + EntityUtil.getTableName(clazz) + " WHERE " + criteria, clazz,
                args);
    }

    public long insert(String table, Map<String, Object> data) throws Exception {
        SimpleJdbcInsert sjdbc = new SimpleJdbcInsert(this);
        return sjdbc.executeAndReturnKey(data).longValue();
    }

    public long insert(Object entity) throws Exception {

        EntityDescriptor ed = EntityUtil.getDescriptor(entity, colInfo -> colInfo.isInsertable() && colInfo.hasValue());
        SimpleJdbcInsert sjdbc = new SimpleJdbcInsert(this);

        Map<String, Object> map = new HashMap<String, Object>();

        for (ColumnInfo ci : ed.getColumns()) {
            map.put(ci.getName(), ci.getValue());
        }

        return sjdbc.executeAndReturnKey(map).longValue();

    }

    /**
     * 
     * @param clazz
     * @param id
     * @return
     * @throws Exception
     */
    public int delete(Class<?> clazz, Object id) throws Exception {
        EntityDescriptor ed = EntityUtil.getDescriptor(clazz);
        Util.throwIf(!ed.hasId(), "There is no id column");
        String sql = "DELETE FROM " + ed.getTableName() + " WHERE " + ed.getIdColumn().getName() + "=?";
        log.info(sql);
        return this.update(sql, id);
    }

    /**
     * 
     * @param entity
     * @return
     * @throws Exception
     */
    public int delete(Object entity) throws Exception {
        EntityDescriptor ed = EntityUtil.getDescriptor(entity, null);
        Util.throwIf(!ed.hasId(), "There is no id column");
        String sql = "DELETE FROM " + ed.getTableName() + " WHERE " + ed.getIdColumn().getName() + "=?";
        log.info(sql + ", id:" + ed.getIdColumn().getValue());
        return this.update(sql, ed.getIdColumn().getValue());
    }

    /**
     * 
     * @param entity
     * @param criteria
     * @param args
     * @return
     * @throws Exception
     */
    public int delete(Object entity, String criteria, Object... args) throws Exception {
        EntityDescriptor ed = EntityUtil.getDescriptor(entity.getClass());
        String sql = "DELETE FROM " + ed.getTableName() + " WHERE " + criteria;
        log.info(sql);
        return this.update(sql, args);
    }

    /**
     * 
     * @param entity
     * @return
     * @throws Exception
     */
    public int update(Object entity) throws Exception {

        EntityDescriptor ed = EntityUtil.getDescriptor(entity, colInfo -> colInfo.isInsertable() && colInfo.hasValue());

        Util.throwIf(!ed.hasId(), "There is no id column");

        return _update(ed, ed.getIdColumn().getName() + "=?", ed.getIdColumn().getValue());
    }

    /**
     * 
     * @param entity
     * @param creteria
     * @param args
     * @return
     * @throws Exception
     */
    public int update(Object entity, String creteria, Object... args) throws Exception {
        EntityDescriptor ed = EntityUtil.getDescriptor(entity, colInfo -> colInfo.isInsertable() && colInfo.hasValue());
        return _update(ed, creteria, args);
    }

    /**
     * ------------------- PRIVATE METHODS -------------------
     */
    private int _update(EntityDescriptor ed, String criteria, Object... params) throws Exception {

        Object[] values = new Object[ed.getColumns().size() + params.length];

        String col = "";
        int i = 0;
        for (ColumnInfo ci : ed.getColumns()) {
            col += "," + ci.getName() + "=?";
            values[i++] = ci.getValue();
        }

        for (Object obj : params) {
            values[i++] = obj;
        }

        String sql = "UPDATE " + ed.getTableName() + " SET " + col.substring(1) + " WHERE " + criteria;
        return this.update(sql, values);
    }

}
