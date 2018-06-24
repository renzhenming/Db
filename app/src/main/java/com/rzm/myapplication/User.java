package com.rzm.myapplication;

import com.rzm.commonlibrary.general.sqlite.annotation.DbTable;

@DbTable(value = "user")
public class User {

    public String name;

    public String password;
}
