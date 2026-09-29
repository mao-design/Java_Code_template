package com.example.shopping.common.constant;


/**
 * 系统公共常量。
 *
 * 注意：
 *
 * 只有多个业务模块都会使用的常量，
 * 才应该放到shopping-common。
 *
 *
 * 例如下面这种不要放这里：
 *
 * PRODUCT_STATUS_ONLINE
 *
 * 因为它属于商品模块。
 */
public final class CommonConstants {


    /**
     * 默认页码。
     */
    public static final long DEFAULT_PAGE = 1L;


    /**
     * 默认分页大小。
     */
    public static final long DEFAULT_PAGE_SIZE = 20L;


    /**
     * 接口允许的最大分页大小。
     *
     * 防止客户端：
     *
     * ?size=99999999
     *
     * 一次把数据库大量数据查出来。
     */
    public static final long MAX_PAGE_SIZE = 100L;


    /**
     * private,工具常量类禁止实例化。
     */
    private CommonConstants() {

    }

}