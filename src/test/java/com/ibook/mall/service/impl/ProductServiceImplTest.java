package com.ibook.mall.service.impl;

import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.vo.ProductDetailVo;
import com.ibook.mall.vo.ResponseVo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductServiceImplTest {

    @Autowired
    private ProductServiceImpl productServiceImpl;

    @Test
    public void detail() {
        ResponseVo<ProductDetailVo> responseVo = productServiceImpl.detail(26);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }
}