package com.rzm.commonlibrary.general.sqlite;

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
}
