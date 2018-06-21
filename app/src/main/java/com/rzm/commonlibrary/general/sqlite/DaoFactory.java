package com.rzm.commonlibrary.general.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

public class DaoFactory {

    /**
     * 数据库地址
     */
    private String dbPath;

    private SQLiteDatabase sqLiteDatabase;

    private DaoFactory(){
        dbPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"user.db";
        openDatabase();
    }

    private static class Holder{
        private static DaoFactory instance = new DaoFactory();
    }

    public static DaoFactory getInstance(){
        return Holder.instance;
    }

    private void openDatabase() {
        this.sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dbPath,null);
    }

    /**
     *
     * @param clazz  bean对象对应的dao
     * @param entity bean对象class
     * @param <Z>
     * @param <R>
     * @return
     */
    public synchronized <Z extends Dao<R>,R> Z getDataHelper(Class<Z> clazz, Class<R> entity){
        if (clazz == null || entity == null){
            throw new NullPointerException("is null");
        }
        Dao dao = null;
        try {
            dao = clazz.newInstance();
            dao.init(entity,sqLiteDatabase);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (Z) dao;
    }

}
