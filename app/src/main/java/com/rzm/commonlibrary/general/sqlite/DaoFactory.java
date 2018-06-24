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

    //内部类实现单例
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
     * @param daoClazz  bean对象对应的dao
     * @param entityClazz bean对象class
     * @param <Z>
     * @param <R>
     * @return
     */
    public synchronized <Z extends Dao<R>,R> Z getDataHelper(Class<Z> daoClazz, Class<R> entityClazz){
        if (daoClazz == null || entityClazz == null){
            throw new NullPointerException("is null");
        }
        Dao dao = null;
        try {
            dao = daoClazz.newInstance();
            dao.init(entityClazz,sqLiteDatabase);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (Z) dao;
    }

}
