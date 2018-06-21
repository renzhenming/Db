package com.rzm.commonlibrary.general.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.rzm.commonlibrary.general.sqlite.annotation.DbField;
import com.rzm.commonlibrary.general.sqlite.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class Dao<R> implements IDao<R>{

    /**
     * 持有数据库操作类的引用
     */
    private SQLiteDatabase database;

    /**
     * 保证只初始化一次
     */
    private boolean hasInit = false;

    /**
     * 持有数据库表所对应的java类型引用
     */
    private Class<R> entityClass;

    /**
     * 表名与成员变量的映射关系
     * key 表名
     * value Field
     */
    private HashMap<String,Field> cacheMap;

    /**
     * 表名
     */
    private String tableName;

    protected synchronized boolean init(Class<R> entity, SQLiteDatabase database){
        if (!hasInit){
            this.entityClass = entity;
            this.database = database;

            //获取表名
            if (entity.getAnnotation(DbTable.class) == null){
                //没有设置类注解DbTable,以类名的字符串形式作为表名
                tableName = entity.getClass().getSimpleName();
            }else{
                tableName = entity.getAnnotation(DbTable.class).value();
            }

            //安全性验证
            if (!database.isOpen()){
                return false;
            }

            if (!TextUtils.isEmpty(createTable())){
                database.execSQL(createTable());
            }

            initCacheMap();
            hasInit = true;
        }
        return hasInit;
    }

    /**
     * 维护映射关系
     */
    private void initCacheMap() {
        String sql ="select * from "+this.tableName+" limit 1 , 0";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql,null);

            //获取表的所有的列
            String [] columnNames = cursor.getColumnNames();

            //获取到bean的所有fields
            Field[] columnFields = entityClass.getFields();

            for (Field field: columnFields){
                field.setAccessible(true);
            }

            //开始找对应关系
            for (String columnName : columnNames){
                Field columnField = null;
                for (Field field: columnFields){
                    String fieldName = null;
                    if (field.getAnnotation(DbField.class) != null){
                        fieldName = field.getAnnotation(DbField.class).value();
                    }else{
                        fieldName = field.getName();
                    }

                    if (columnName.equals(fieldName)){
                        columnField = field;
                        break;
                    }
                }
                if (columnField != null){
                    cacheMap.put(columnName,columnField);
                }
            }
        }catch (Exception e){

        }finally {
            cursor.close();
        }
    }

    /**
     * 创建具体的表的操作交给具体的bean
     * @return
     */
    protected abstract String createTable();

    @Override
    public Long insert(R entity) {
        if (entity == null) return 0l;
        Map<String,String> map = getValues(entity);
        ContentValues values=getContentValues(map);
        Long result =database.insert(tableName,null,values);
        return result;
    }

    /**
     * 将hashmap中的值放入ContentValue
     * @param map
     * @return
     */
    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues=new ContentValues();
        Set keys=map.keySet();
        Iterator<String> iterator=keys.iterator();
        while (iterator.hasNext())
        {
            String key=iterator.next();
            String value=map.get(key);
            if(value!=null)
            {
                contentValues.put(key,value);
            }
        }

        return contentValues;
    }

    /**
     * 将bean对象通过反射获取到它的属性和值存入hashmap
     * @param entity
     * @return
     */
    private Map<String, String> getValues(R entity) {
        HashMap<String,String> result=new HashMap<>();
        Iterator<Field> iterator = cacheMap.values().iterator();
        while(iterator.hasNext()){
            Field columnField = iterator.next();
            String cacheKey = null;
            String cacheValue = null;
            if (columnField.getAnnotation(DbField.class) != null){
                cacheKey = columnField.getAnnotation(DbField.class).value();
            }else{
                cacheKey = columnField.getName();
            }

            try {
                if (columnField.get(entity) == null){
                    continue;
                }
                cacheValue = columnField.get(entity).toString();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            result.put(cacheKey,cacheValue);
        }
        return result;
    }

    @Override
    public Long update(R entity, R where) {
        return null;
    }
}
