package com.ibook.mall;

//import org.junit.jupiter.api.Test;
import com.ibook.mall.dao.CategoryMapper;
import com.ibook.mall.pojo.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Test;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MallApplicationTests {

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    public void contextLoads() {
        Category category = categoryMapper.findById(100001);
        System.out.println(category.toString());
    }

    @Test
    public void queryByIdTest() {
        Category category = categoryMapper.queryById(100001);
        System.out.println(category.toString());
    }

}
