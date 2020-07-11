package com.ibook.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

//根据文档:https://git.imooc.com/coding-392/doc/src/master/api/%e8%b4%ad%e7%89%a9%e8%bd%a6.md
//进行这个vo对象字段的填写

@Data
public class CartVo {

    private List<CartProductVo> cartProductVoList;

    private Boolean selectedAll;

    private BigDecimal cartTotalPrice;

    private Integer cartTotalQuantity;

}
