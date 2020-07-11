package com.ibook.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

//根据文档:https://git.imooc.com/coding-392/doc/src/master/api/%e8%b4%ad%e7%89%a9%e8%bd%a6.md
//进行这个vo对象字段的填写

@Data
public class CartProductVo {

    private Integer productId;

    //购买的数量(与库存不同)
    private Integer quantity;

    private String productName;

    private String productSubtitle;

    private String productMainImage;

    private BigDecimal productPrice;

    private Integer productStatus;

    //总价,等于quantity * productPrice
    private BigDecimal productTotalPrice;

    private Integer productStock;

    //商品是否选中
    private boolean productSelected;

    public CartProductVo(Integer productId, Integer quantity, String productName, String productSubtitle, String productMainImage, BigDecimal productPrice, Integer productStatus, BigDecimal productTotalPrice, Integer productStock, boolean productSelected) {
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.productSubtitle = productSubtitle;
        this.productMainImage = productMainImage;
        this.productPrice = productPrice;
        this.productStatus = productStatus;
        this.productTotalPrice = productTotalPrice;
        this.productStock = productStock;
        this.productSelected = productSelected;
    }
}
