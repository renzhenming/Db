package com.rzm.commonlibrary.general.sqlite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 封装修改语句
 */
class Condition {
    /**
     * 查询条件
     * name=? && password =?
     */
    private String whereClause;

    private String[] whereArgs;

    public Condition(Map<String, String> whereClause) {
        ArrayList list = new ArrayList();
        StringBuilder stringBuilder = new StringBuilder();

        //在前边拼接一个1=1只是一个占位的作用，下边循环拼接每次都会加一个
        //and ,为了减少判断，加上一个-1=-1，如果不加就会显示 and name = ? ..
        stringBuilder.append(" -1=-1 ");
        Set keys = whereClause.keySet();
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = whereClause.get(key);

            if (value != null) {
                /*
                  拼接条件查询语句
                  1=1 and name =? and password=?
                  */
                stringBuilder.append(" and " + key + " =?");
                /**
                 * ？----》value
                 */
                list.add(value);
            }
        }
        this.whereClause = stringBuilder.toString();
        this.whereArgs = (String[]) list.toArray(new String[list.size()]);

    }

    public String[] getWhereArgs() {
        return whereArgs;
    }

    public String getWhereClause() {
        return whereClause;
    }
}