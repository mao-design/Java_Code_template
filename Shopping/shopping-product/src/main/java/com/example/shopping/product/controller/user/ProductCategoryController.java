package com.example.shopping.product.controller.user;


import com.example.shopping.common.result.Result;
import com.example.shopping.product.model.vo.ProductCategoryVO;
import com.example.shopping.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 商品分类前台接口
 */
@RestController
@RequestMapping("/product/category")
@RequiredArgsConstructor
public class ProductCategoryController {


    private final ProductCategoryService categoryService;



    /**
     * 商品导航分类树
     */
    @GetMapping("/tree")
    public Result<List<ProductCategoryVO>> tree(){

        return Result.success(
                categoryService.treeCategory()
        );

    }

}