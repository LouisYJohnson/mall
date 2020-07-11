package com.ibook.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ibook.mall.dao.ProductMapper;
import com.ibook.mall.enums.ProductStatusEnum;
import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.pojo.Product;
import com.ibook.mall.service.IProductService;
import com.ibook.mall.vo.ProductDetailVo;
import com.ibook.mall.vo.ProductVo;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private CategoryServiceImpl categoryServiceImpl;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ResponseVo<PageInfo> list(Integer categoryId, Integer pageNum, Integer pageSize) {
        Set<Integer> categoryIdSet = new HashSet<>();
        //如果并没有传入categoryId,应该返回所有的商品,而不是返回一个null
        //如果categoryId不是null才有查询的必要,并将categoryId放入这个set中
        //如果categoryId是null的话就把空的set放到查询所有商品的方法中
        //如果将null放到set中,set就会有一个null作为元素,set的size为1
        if (categoryId != null) {
            //通过categoryServiceImpl找到传入id的所有子产品的id,
            // 然后再将自身加入到这个set中(自己也算是产品集合的一部分)
            categoryServiceImpl.findSubCategoryId(categoryId, categoryIdSet);
            categoryIdSet.add(categoryId);
        }

        //在这里设置分页
        PageHelper.startPage(pageNum, pageSize);

        //通过categoryIdSet去查询所有的商品
        List<Product> productList = productMapper.selectByCategoryIdSet(categoryIdSet);
        List<ProductVo> productVoList = productList.stream()
                .map(e -> {
                    ProductVo productVo = new ProductVo();
                    BeanUtils.copyProperties(e, productVo);
                    return productVo;
                })
                .collect(Collectors.toList());

        //在这里设置分页后要返回的json数据格式
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productVoList);
        return ResponseVo.success(pageInfo);
    }

    @Override
    public ResponseVo<ProductDetailVo> detail(Integer productId) {
        Product product = productMapper.selectByPrimaryKey(productId);

        //如果下架或者删除要返回失败的json数据
        //这里一定要这么写而不是图方便写成如果是商品还在(状态为ON_SALE)就继续,假设当前状态多了一个
        //促销,促销也是可以卖的,但是这种图方便的写法还需要再改
        //注意,这里如果查询不存在的商品的话,会返回服务端错误,
        //这是因为返回的product为null,null是不具备任何方法的,对其进行调用不存在的对象方法
        //就会抛出RuntimeException进而被项目中的统一异常处理器处理掉
        //所以我们首先判断查到的product是否为null,将其写在最前面,
        //这样product如果为null就不会执行后面的判断语法,直接进入返回错误信息的流程
        //除此之外,数据库如果报错的话,也会抛出RuntimeException异常
        if (product == null
                ||
                product.getStatus().equals(ProductStatusEnum.OFF_SALE.getCode())
                ||
                product.getStatus().equals(ProductStatusEnum.DELETE.getCode())
                ) {
            return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE);
        }
        //走到这里说明没有下架或者删除,继续运行
        //虽然两个对象的字段都是一模一样的,但是一定要转换一下才能有更好的解耦性
        ProductDetailVo productDetailVo = new ProductDetailVo();
        BeanUtils.copyProperties(product, productDetailVo);
        return ResponseVo.success(productDetailVo);
    }
}
