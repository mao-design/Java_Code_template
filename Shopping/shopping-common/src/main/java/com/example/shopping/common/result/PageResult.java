package com.example.shopping.common.result;

import lombok.Getter;

import java.util.Collections;
import java.util.List;


/**
 * 通用分页结果。
 *
 * 不直接把 MyBatis-Plus 的 Page 对象返回给前端。
 *
 * 原因：
 *
 * 1. 避免接口层和MyBatis-Plus强绑定
 * 2. 将来换数据库框架时接口不用跟着修改
 * 3. 返回字段由我们自己控制
 *
 *
 * @param <T> 当前页数据类型
 */

@Getter
public class PageResult<T> {


    /**
     * 当前页。
     */
    private final long page;


    /**
     * 每页数量。
     */
    private final long size;


    /**
     * 总记录数。
     */
    private final long total;


    /**
     * 当前页数据。
     */
    private final List<T> records;


    public PageResult(
            long page,
            long size,
            long total,
            List<T> records
    ) {

        this.page = page;

        this.size = size;

        this.total = total;

        /*
         * 避免records出现null。
         *
         * 前端更加喜欢：
         *
         * "records": []
         *
         * 而不是：
         *
         * "records": null
         */
        this.records = records == null ? Collections.emptyList() : records;
    }

}
