package com.ibook.mall.service;

import com.github.pagehelper.PageInfo;
import com.ibook.mall.vo.ProductDetailVo;
import com.ibook.mall.vo.ResponseVo;

public interface IProductService {

    ResponseVo<PageInfo> list(Integer categoryId, Integer pageNum, Integer pageSize);

    ResponseVo<ProductDetailVo> detail(Integer productId);
}
