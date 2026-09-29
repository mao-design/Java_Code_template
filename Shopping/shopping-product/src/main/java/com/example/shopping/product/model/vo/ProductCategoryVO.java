package com.example.shopping.product.model.vo;


import lombok.Data;

import java.util.List;


/**
 * 商品分类返回对象
 *
 * 用于：

 * 分类树展示
 *
 */
@Data
public class ProductCategoryVO {


    /**
     * 分类ID
     */
    private Long id;



    /**
     * 分类名称
     */
    private String name;



    /**
     * 父分类ID
     */
    private Long parentId;



    /**
     * 层级
     */
    private Integer level;



    /**
     * 排序
     */
    private Integer sort;



    /**
     * 图标
     */
    private String icon;



    /**
     * 子分类
     *
     * 分类树核心字段
     */
    private List<ProductCategoryVO> children;


}
