package com.rzm.commonlibrary.general.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.rzm.commonlibrary.general.sqlite.annotation.DbField;
import com.rzm.commonlibrary.general.sqlite.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    protected synchronized boolean init(Class<R> entityClazz, SQLiteDatabase database){
        if (!hasInit){
            this.entityClass = entityClazz;
            this.database = database;

            //获取表名
            if (entityClazz.getAnnotation(DbTable.class) == null){
                //没有设置类注解DbTable,以类名的字符串形式作为表名
                tableName = entityClazz.getSimpleName();
            }else{
                tableName = entityClazz.getAnnotation(DbTable.class).value();
            }

            //安全性验证
            if (!database.isOpen()){
                return false;
            }

            if (!TextUtils.isEmpty(createTable(tableName))){
                database.execSQL(createTable(tableName));
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
        cacheMap = new HashMap<>();
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
            if (cursor != null) {
                cursor.close();
            }
        }
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

        Set<String> keys = cacheMap.keySet();
        for (String key : keys) {
            Field columnField = cacheMap.get(key);
            String cacheKey = key;

            String cacheValue = null;
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

        /*Iterator<Field> iterator = cacheMap.values().iterator();
        while(iterator.hasNext()){
            Field columnField = iterator.next();
            String cacheKey = null;

            if (columnField.getAnnotation(DbField.class) != null){
                cacheKey = columnField.getAnnotation(DbField.class).value();
            }else{
                cacheKey = columnField.getName();
            }
            String cacheValue = null;
            try {
                if (columnField.get(entity) == null){
                    continue;
                }
                cacheValue = columnField.get(entity).toString();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            result.put(cacheKey,cacheValue);
        }*/
        return result;
    }

    /**
     * 创建具体的表的操作交给具体的bean
     * @return
     * @param tableName
     */
    protected abstract String createTable(String tableName);

    @Override
    public Long insert(R entity) {
        if (entity == null) return 0l;
        Map<String,String> map = getValues(entity);
        ContentValues values=getContentValues(map);
        Long result =database.insert(tableName,null,values);
        return result;
    }

    @Override
    public Long delete(R where) {
        Map<String,String> deleteValues = getValues(where);
        Condition condition = new Condition(deleteValues);
        long result = database.delete(tableName,condition.getWhereClause(),condition.getWhereArgs());
        return result;
    }

    @Override
    public Long update(R entity, R where) {
        long result = -1;
        Map<String,String> updateClause = getValues(entity);

        Map<String,String> whereClause = getValues(where);
        Condition condition = new Condition(whereClause);
        ContentValues values = getContentValues(updateClause);
        result = database.update(tableName,values,condition.getWhereClause(),condition.getWhereArgs());
        //result = database.updateWithOnConflict()
        return result;
    }

    @Override
    public List<R> query(R where) {
        return query(where,null,null,null);
    }

    @Override
    public List<R> query(R where, String orderBy, Integer startIndex, Integer limit) {
        Map map=getValues(where);

        String limitString=null;
        if(startIndex!=null&&limit!=null)
        {
            limitString=startIndex+" , "+limit;
        }

        Condition condition=new Condition(map);
        Cursor cursor=database.query(tableName,null,condition.getWhereClause()
                ,condition.getWhereArgs(),null,null,orderBy,limitString);
        List<R> result=getResult(cursor,where);
        cursor.close();
        return result;
    }

    private List<R> getResult(Cursor cursor, R where) {
        ArrayList list=new ArrayList();

        Object item;
        while (cursor.moveToNext())
        {
            try {
                item=where.getClass().newInstance();
                /**
                 * 列名  name
                 * 成员变量名  Filed;
                 */
                Iterator iterator=cacheMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry entry= (Map.Entry) iterator.next();
                    /**
                     * 得到列名
                     */
                    String colomunName= (String) entry.getKey();
                    /**
                     * 然后以列名拿到  列名在游标的位子
                     */
                    Integer colmunIndex=cursor.getColumnIndex(colomunName);

                    Field field= (Field) entry.getValue();

                    Class type=field.getType();
                    if(colmunIndex!=-1)
                    {
                        if(type==String.class)
                        {
                            //反射方式赋值
                            field.set(item,cursor.getString(colmunIndex));
                        }else if(type==Double.class)
                        {
                            field.set(item,cursor.getDouble(colmunIndex));
                        }else  if(type==Integer.class)
                        {
                            field.set(item,cursor.getInt(colmunIndex));
                        }else if(type==Long.class)
                        {
                            field.set(item,cursor.getLong(colmunIndex));
                        }else  if(type==byte[].class)
                        {
                            field.set(item,cursor.getBlob(colmunIndex));
                            /*
                            不支持的类型
                             */
                        }else {
                            continue;
                        }
                    }

                }
                list.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return list;
    }
}
