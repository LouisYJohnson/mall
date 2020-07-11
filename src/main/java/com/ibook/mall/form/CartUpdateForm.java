package com.ibook.mall.form;

import lombok.Data;

//根据文档https://git.imooc.com/coding-392/doc/src/master/api/%e8%b4%ad%e7%89%a9%e8%bd%a6.md进行开发
//由于都是非必填参数,所以不需要表单校验
@Data
public class CartUpdateForm {

    private Integer quantity;

    private Boolean selected;
}
