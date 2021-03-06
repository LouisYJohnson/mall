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
    //方法目的:传入一个产品的id将其相关(所有子类类目的商品全都查出来,拿着1级查2级,不可能是递归,别和ServiceImpl中的搞混了)
    //https://blog.csdn.net/mrqiang9001/article/details/79520436
    //有了@Param注解,在xml中不用写parameterType,直接用注解中的别名就可以了,一般是在传多个参数的时候用
    //这里只是展示一下
    List<Product> selectByCategoryIdSet(@Param("categoryIdSet") Set<Integer> categoryIdSet);

    List<Product> selectByProductIdSet(@Param("categoryIdSet") Set<Integer> productIdSet);
}