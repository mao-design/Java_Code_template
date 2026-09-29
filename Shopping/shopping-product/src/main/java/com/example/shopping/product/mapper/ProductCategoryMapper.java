package com.example.shopping.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shopping.product.model.entity.ProductCategoryDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategoryDO> {
}
