package com.example.shopping.product.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shopping.product.mapper.ProductCategoryMapper;
import com.example.shopping.product.model.dto.ProductCategoryDTO;
import com.example.shopping.product.model.entity.ProductCategoryDO;
import com.example.shopping.product.model.vo.ProductCategoryVO;
import com.example.shopping.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper categoryMapper;

//    新增分类
    @Override
    public void createCategory(ProductCategoryDTO dto) {
        ProductCategoryDO category = new ProductCategoryDO();
        BeanUtils.copyProperties(dto, category);
        if (dto.getParentId() != null) {
            category.setParentId(0L);
        }
        categoryMapper.insert(category);
    }

//    修改分类
    @Override
    public void updateCategory(ProductCategoryDTO dto) {
        ProductCategoryDO category = new ProductCategoryDO();
        BeanUtils.copyProperties(dto, category);
        categoryMapper.updateById(category);
    }

//    删除分类
    @Override
    public void deleteCategory(Long id) {
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
        categoryMapper.deleteById(id);
    }

    // 查询分类列表
    @Override
    public List<ProductCategoryVO> listCategory() {

        List<ProductCategoryDO> list = categoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategoryDO>()
                        .orderByAsc(ProductCategoryDO::getSort)
        );

        List<ProductCategoryVO> convert;

        return convert(list);
    }

    // 查询分类树
    @Override
    public List<ProductCategoryVO> treeCategory() {
        List<ProductCategoryDO> list = categoryMapper.selectList(null);
        List<ProductCategoryVO> tree = convert(list);

        return buildTree(
                tree, 0L
        );
    }

    private List<ProductCategoryVO> convert(List<ProductCategoryDO> list) {
        List<ProductCategoryVO> result = new ArrayList<>();

        for (ProductCategoryDO item : list) {
            ProductCategoryVO vo = new ProductCategoryVO();
            BeanUtils.copyProperties(item, vo);
            vo.setChildren(new ArrayList<>());
            result.add(vo);
        }
        return result;
    }

    private List<ProductCategoryVO> buildTree(List<ProductCategoryVO> list, Long parentId) {
        List<ProductCategoryVO> result = new ArrayList<>();
        for (ProductCategoryVO item : list) {
            if (item.getParentId().equals(parentId)) {
                item.setChildren(buildTree(list, item.getId()));
                result.add(item);
            }
        }
        return result;
    }
}
