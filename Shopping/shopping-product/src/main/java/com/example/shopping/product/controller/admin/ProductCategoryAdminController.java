package com.example.shopping.product.controller.admin;

import com.example.shopping.common.result.Result;
import com.example.shopping.product.model.dto.ProductCategoryDTO;
import com.example.shopping.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product/category")
@RequiredArgsConstructor
public class ProductCategoryAdminController {
    private final ProductCategoryService categoryService;

//    新增分类
    @PostMapping
    public Result<Void> create(
            @Valid
            @RequestBody
            ProductCategoryDTO dto
    ) {
        categoryService.createCategory(dto);
        return Result.success();
    }
}
