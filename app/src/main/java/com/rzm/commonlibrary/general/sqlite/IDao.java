package com.rzm.commonlibrary.general.sqlite;

import java.util.List;

public interface IDao<R> {

    /**
     * 插入表数据
     * @param entity
     * @return
     */
    Long insert(R entity);

    /**
     * 更新表数据
     * @param entity
     * @param where
     * @return
     */
    Long update(R entity,R where);

    /*
     * 删除表数据
     * @param where
     * @return
     */
    Long delete(R where);

    /**
     * 查询表数据
     */
    List<R> query(R where);


    List<R> query(R where, String orderBy, Integer startIndex, Integer limit);

}
