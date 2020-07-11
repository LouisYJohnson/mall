package com.ibook.mall.service.impl;

import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.enums.RoleEnum;
import com.ibook.mall.pojo.User;
import com.ibook.mall.vo.ResponseVo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class UserServiceImplTest {

    public static final String USERNAME = "jack";

    public static final String PASSWORD = "123456";

    @Autowired
    private UserServiceImpl userService;

//    @Test
    @Before //这个注解表示所有的单元测试之前都要运行这个方法
    public void register() {
        User user = new User(USERNAME, PASSWORD, "jack@qq.com", RoleEnum.CUSTOMER.getCode());
        userService.register(user);
    }

    @Test
    public void login() {
        ResponseVo<User> responseVo = userService.login(USERNAME, PASSWORD);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }
}