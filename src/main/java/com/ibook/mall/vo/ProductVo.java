package com.ibook.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

//根据文档https://git.imooc.com/coding-392/doc/src/master/api来对应responseVo对象的参数
@Data
public class ProductVo {
    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;

    private String mainImage;

    private BigDecimal price;

    private Integer status;
}
