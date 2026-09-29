package com.example.shopping.product.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 商品分类实体。
 *
 * 对应数据库：
 *
 * product_category
 *
 */
@Data
@TableName("product_category")
public class ProductCategoryDO {


    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;



    /**
     * 父分类ID
     *
     * 一级分类：
     *
     * parentId = 0
     */
    private Long parentId;



    /**
     * 分类名称
     */
    private String name;



    /**
     * 分类层级
     *
     * 1 一级分类
     * 2 二级分类
     * 3 三级分类
     */
    private Integer level;



    /**
     * 排序字段
     */
    private Integer sort;



    /**
     * 状态
     *
     * 1 启用
     * 0 禁用
     */
    private Integer status;



    /**
     * 分类图片
     */
    private String icon;



    /**
     * 创建时间
     */
    private LocalDateTime createTime;



    /**
     * 修改时间
     */
    private LocalDateTime updateTime;



    /**
     * 逻辑删除
     *
     * 0 未删除
     * 1 删除
     */
    @TableLogic
    private Integer deleted;

}
