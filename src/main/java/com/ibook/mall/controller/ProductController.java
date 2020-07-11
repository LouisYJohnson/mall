package com.ibook.mall.controller;

import com.github.pagehelper.PageInfo;
import com.ibook.mall.service.impl.ProductServiceImpl;
import com.ibook.mall.vo.ProductDetailVo;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @Autowired
    private ProductServiceImpl productServiceImpl;

    //GET请求参数只能用?和&跟到链接后面,不能传入json格式的数据,接不到
    //所以使用@RequestParam而不是@RequestBody注解
    //不要忘记去配置拦截(不需要登录就可以访问)
    @GetMapping("/products")
    public ResponseVo<PageInfo> list(@RequestParam(required = false) Integer categoryId,
                                     @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                     @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return productServiceImpl.list(categoryId, pageNum, pageSize);
    }

    @GetMapping("/products/{productId}")
    //参数在url中,所以用@PathVariable注解
    public ResponseVo<ProductDetailVo> detail(@PathVariable Integer productId) {
        return productServiceImpl.detail(productId);
    }
}
