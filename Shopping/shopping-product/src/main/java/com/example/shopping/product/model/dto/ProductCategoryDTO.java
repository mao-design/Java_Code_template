package com.example.shopping.product.model.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 商品分类请求参数
 *
 * 用于：

 * 新增分类
 * 修改分类
 *
 */
@Data
public class ProductCategoryDTO {


    /**
     * 分类ID
     *
     * 修改时使用
     */
    private Long id;



    /**
     * 父分类ID
     *
     * 一级分类：
     *
     * 0
     */
    private Long parentId;



    /**
     * 分类名称
     */
    @NotBlank(
            message = "分类名称不能为空"
    )
    private String name;



    /**
     * 分类层级
     *
     * 1 一级
     * 2 二级
     * 3 三级
     */
    @NotNull(
            message = "分类层级不能为空"
    )
    private Integer level;



    /**
     * 排序
     */
    private Integer sort;



    /**
     * 状态
     *
     * 1启用
     * 0禁用
     */
    private Integer status;



    /**
     * 分类图片
     */
    private String icon;

}