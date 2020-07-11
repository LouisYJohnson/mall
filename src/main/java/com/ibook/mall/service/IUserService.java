package com.ibook.mall.service;

import com.ibook.mall.pojo.User;
import com.ibook.mall.vo.ResponseVo;


public interface IUserService {

    /**
     * 注册
     * https://git.imooc.com/coding-392/doc/src/master/api/用户.md
     * 根据这个说明文档,来设计方法的传入参数
     */
    ResponseVo<User> register(User user);

    /**
     * 登录
     */
    ResponseVo<User> login(String username, String password);

}
