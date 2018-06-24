package com.rzm.myapplication;

import com.rzm.commonlibrary.general.sqlite.Dao;


public class UserDao extends Dao {
    @Override
    protected String createTable(String tableName) {
        return "create table if not exists "+tableName+"(name text,password text)";
    }

}
