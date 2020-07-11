package com.ibook.mall.vo;

import lombok.Data;

import java.util.List;

//此类用于返回查询类目对象
//根据开发文档:https://git.imooc.com/coding-392/doc/src/master/api/%e7%b1%bb%e7%9b%ae.md
//与数据库中的mall_category
@Data
public class CategoryVo {

    private Integer id;

    private Integer parentId;

    private String name;

    private Integer sortOrder;

    //返回的子类目又是自己
    private List<CategoryVo> subCategories;
}
