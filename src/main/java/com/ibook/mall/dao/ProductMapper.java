package com.ibook.mall.dao;

import com.ibook.mall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    //一旦mybatis中传入的数据类型不是一般的数据类型,需要单独加注解
    //方法目的:传入一个产品的id将其相关(所有子类类目的商品全都查出来)
    List<Product> selectByCategoryIdSet(@Param("categoryIdSet") Set<Integer> categoryIdSet);
}