package com.example.shopping.product.service;

import com.example.shopping.product.model.dto.ProductCategoryDTO;
import com.example.shopping.product.model.entity.ProductCategoryDO;
import com.example.shopping.product.model.vo.ProductCategoryVO;

import java.util.List;

/**
 * 商品分类服务接口
 *
 * 负责商品分类业务处理。
 *
 * Controller 不直接操作 Mapper。
 *
 * 所有业务逻辑放 Service。
 */
public interface ProductCategoryService {
    /*
    * 新增商品分类
    * */
    void createCategory(ProductCategoryDTO dto);

    /*
    * 修改商品分类
    * */
    void updateCategory(ProductCategoryDTO dto);

    /*
    * 删除商品分类
    * */
    void deleteCategory(Long id);

    /**
     * 查询分类列表
     *
     * 后台管理使用。
     *
     * 例如：
     *
     * 一级分类列表
     *
     * 手机
     * 电脑
     * 家电
     *
     */
    List<ProductCategoryVO> listCategory();

    /**
     * 查询分类树
     *
     * 前端商品分类导航使用。
     *
     * 返回结构：
     *
     * 手机
     *   |
     *   安卓手机
     *        |
     *        小米
     *
     */
    List<ProductCategoryVO> treeCategory();
}
