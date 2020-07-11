package com.ibook.mall;

//import org.junit.jupiter.api.Test;
import com.ibook.mall.dao.CategoryMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MallApplicationTests {

    @Autowired
    private CategoryMapper categoryMapper;


}
