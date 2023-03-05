package com.nihalsoft.java.jdbc.orm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.nihalsoft.java.jdbc.orm.common.DataMap;
import com.nihalsoft.java.jdbc.orm.common.EntityDescriptor;
import com.nihalsoft.java.jdbc.orm.common.EntityUtil;
import com.nihalsoft.java.jdbc.orm.common.SQLDataMapper;
import com.nihalsoft.java.jdbc.orm.common.SysEntity;
import com.nihalsoft.java.jdbc.orm.result.handler.BeanListHandler;
import com.nihalsoft.java.jdbc.orm.result.handler.BeanMapper;
import com.nihalsoft.java.jdbc.orm.result.handler.BeanProcessor;
import com.nihalsoft.java.jdbc.orm.result.handler.BeanResultHandler;
import com.nihalsoft.java.jdbc.orm.result.handler.DataMapMapper;
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

    public Select select() {
        return new Select(this);
    }

    public Select select(String tableName) {
        return new Select(this, tableName);
    }

    public Select select(Class<?> entity) {
        return new Select(this, entity);
    }

    public List<Object[]> queryForObjectList(String sql, Object... args) throws Exception {
        return this.query(sql, new ResultListHandler<Object[]>(new ObjectMapper()), args);
    }

    public Object[] queryForObject(String sql, Object... args) throws Exception {
        return this.query(sql, new ResultHandler<Object[]>(new ObjectMapper()), args);
    }

    public <T> List<T> queryForBeanList(String sql, Class<T> type, Object... args) {
        try {
            return this.query(sql, new BeanListHandler<T>(beanProcessor, type), args);
        } catch (Exception ex) {

        }
        return new ArrayList<>();
    }

    public <T> T queryForBean(String sql, Class<T> type, Object... args) {
        try {
            return this.query(sql, new BeanResultHandler<T>(new BeanMapper<T>(beanProcessor, type)), args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public <T> T queryForBean(Class<T> type, Object id) {
        try {
            EntityDescriptor ed = EntityUtil.getDescriptor(type);
            return this.query("SELECT * FROM " + ed.getTableName() + " WHERE id=?",
                    new BeanResultHandler<T>(new BeanMapper<T>(beanProcessor, type)), id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<DataMap> queryForListOfDataMap(String sql, Object... args) throws DataAccessException {
        return this.query(sql, new SQLDataMapper(), args);
    }

    public DataMap queryForDataMap(String sql, Object... args) throws DataAccessException {
        return this.query(sql, new ResultHandler<DataMap>(new DataMapMapper()), args);
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
        String tn = EntityUtil.getTableName(clazz);
//        Util.throwSqlException(!ed.hasId(), "There is no id column");
        return this.queryForBean("SELECT * FROM " + tn + " WHERE id=?", clazz, id);
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

    public List<Map<String, Object>> findAll(String table) throws Exception {
        return this.queryForList("SELECT * FROM " + table);
    }

    public <T> List<T> findAll(Class<T> clazz, Object id) throws Exception {
        return this.queryForBeanList("SELECT * FROM " + EntityUtil.getTableName(clazz) + " WHERE id=?", clazz, id);
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
    public <T> List<T> findAll(Class<T> clazz, String criteria, Object... args) throws Exception {
        return this.queryForBeanList("SELECT * FROM " + EntityUtil.getTableName(clazz) + " WHERE " + criteria, clazz,
                args);
    }

    public <T> List<T> findAll(String sql, Class<T> clazz, Object... args) throws Exception {
        return this.queryForBeanList(sql, clazz, args);
    }

    public long insert(String table, Map<String, Object> data) throws Exception {
        SimpleJdbcInsert sjdbc = new SimpleJdbcInsert(this);
        return sjdbc.withTableName(table).usingGeneratedKeyColumns("id").executeAndReturnKey(data).longValue();
    }

    public void saveOrUpdateBatch(List<SysEntity> entities) throws Exception {

        // EntityDescriptor ed = EntityUtil.getDescriptor(entities.get(0), colInfo ->
        // colInfo.hasValue());

    }

    public long saveOrUpdate(SysEntity entity, String... cols) {

        EntityDescriptor ed;
        try {
            ed = EntityUtil.getDescriptor(entity, cols);

            if (entity.isNew()) {
                SimpleJdbcInsert sjdbc = new SimpleJdbcInsert(this);

                long id = sjdbc.withTableName(ed.getTableName()) //
                        .usingGeneratedKeyColumns("id") //
                        .executeAndReturnKey(ed.toDataMap()) //
                        .longValue();

                entity.setId(id);

                return id;

            } else {
                String sql = ed.getSqlForUpdate("id=?");
                log.info(sql);
                return this.update(sql, ed.getValues(entity.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean exist(String table, String where, Object... args) throws Exception {
        try {
            return this.queryForObject("SELECT EXISTS(SELECT id FROM " + table + " WHERE " + where + " LIMIT 1)",
                    Long.class, args) == 1;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 
     * @param clazz
     * @param id
     * @return
     * @throws Exception
     */
    public int delete(Class<?> clazz, Object id) throws Exception {
        String tn = EntityUtil.getTableName(clazz);
        return this.update("DELETE FROM " + tn + " WHERE id=?", id);
    }

    public int delete(Class<?> clazz, String criteria, Object... args) throws Exception {
        String tn = EntityUtil.getTableName(clazz);
        return this.update("DELETE FROM " + tn + " WHERE " + criteria, args);
    }

    /**
     * 
     * @param entity
     * @return
     * @throws Exception
     */
    public int delete(SysEntity entity) throws Exception {
//        Util.throwIf(!ed.hasId(), "There is no id column");
        String sql = "DELETE FROM " + entity.getTableName() + " WHERE id=?";
        log.info(sql + ", id:" + entity.getId());
        return this.update(sql, entity.getId());
    }

    /**
     * 
     * @param entity
     * @param criteria
     * @param args
     * @return
     * @throws Exception
     */
    public int delete(SysEntity entity, String criteria, Object... args) throws Exception {
        EntityDescriptor ed = EntityUtil.getDescriptor(entity.getClass());
        String sql = "DELETE FROM " + ed.getTableName() + " WHERE " + criteria;
        log.info(sql);
        return this.update(sql, args);
    }

    /**
     * 
     * @param entity
     * @param creteria
     * @param args
     * @return
     * @throws Exception
     */
    public int update(SysEntity entity, String creteria, Object... args) throws Exception {
        EntityDescriptor ed = EntityUtil.getDescriptor(entity);
        return this.update(ed.getSqlForUpdate(creteria), ed.getValues(args));
    }

    @SuppressWarnings("unchecked")
    public <T> T load(Class<? extends SysEntity> sysEntity, String creteria, Object... params) {
        EntityDescriptor ed = EntityUtil.getDescriptor(sysEntity);
        return (T) this.queryForBean("SELECT * FROM " + ed.getTableName() + " WHERE " + creteria, sysEntity, params);
    }
}
