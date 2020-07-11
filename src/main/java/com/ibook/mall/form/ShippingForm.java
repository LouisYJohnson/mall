package com.ibook.mall.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

//根据文档:https://git.imooc.com/coding-392/doc/src/master/api/%e6%94%b6%e8%b4%a7%e5%9c%b0%e5%9d%80.md
//来对应传入参数的对象
//pojo与form类:
//  pojo是与数据库进行交互的所以需要数据库中所有的字段,将数据库中全部的数据都拿过来
//  form类是与前端交互的,只需要装载前端传入数据对应的字段即可,
//      它的使命是将前端传入的数据通过@RequestBody装入form对象中并当作参数传到方法中去,
//      让方法能够知道前端都传过来了什么数据
@Data
public class ShippingForm {

    @NotBlank
    private String receiverName;

    @NotBlank
    private String receiverPhone;

    @NotBlank
    private String receiverMobile;

    @NotBlank
    private String receiverProvince;

    @NotBlank
    private String receiverCity;

    @NotBlank
    private String receiverDistrict;

    @NotBlank
    private String receiverAddress;

    @NotBlank
    private String receiverZip;

}
