package com.ibook.mall.dao;

import com.ibook.mall.pojo.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

//因为主类中配置了包扫描Mapper,所以这里就不用写@Mapper注解了
//@Mapper
public interface CategoryMapper {
    @Select("select * from mall_category where id = #{id}")
    Category findById(@Param("id") Integer id);

    Category queryById(Integer id);
}
