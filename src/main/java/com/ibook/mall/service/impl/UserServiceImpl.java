package com.ibook.mall.service.impl;

import com.ibook.mall.dao.UserMapper;
import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.enums.RoleEnum;
import com.ibook.mall.pojo.User;
import com.ibook.mall.service.IUserService;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 注册
     * https://git.imooc.com/coding-392/doc/src/master/api/用户.md
     * 根据这个说明文档,来设计方法的传入参数
     * 错误或者正确,都将对应的ResponseVo对象传出去,这样前端就能接收到对应的json数据
     *
     * @param user
     */
    @Override
    public ResponseVo<User> register(User user) {
//        //模拟在注册的时候出现的意外错误,不要效果就注释掉
//        error();

        //校验:
        //username不能重复
        int countByUsername = userMapper.countByUsername(user.getUsername());
        if (countByUsername > 0) {
            //抛出的异常都是在调试的时候用的,真正开发的时候都是给前端返回数据
//            throw new RuntimeException("该username已注册");
            return ResponseVo.error(ResponseEnum.USERNAME_EXIST);
        }
        //email不能重复
        int countByEmail = userMapper.countByEmail(user.getEmail());
        if (countByEmail > 0) {
//            throw new RuntimeException("该email已注册");
            return ResponseVo.error(ResponseEnum.EMAIL_EXIST);
        }

        //role不能为空,所以都默认为普通用户
        user.setRole(RoleEnum.CUSTOMER.getCode());

        //MD5加密密码(spring自带) 摘要
        //MD5严格来说是一种摘要算法
        user.setPassword(DigestUtils.md5DigestAsHex(
                user.getPassword().getBytes(StandardCharsets.UTF_8)));
        //写入数据库
        int resultCount = userMapper.insertSelective(user);
        if (resultCount == 0) {
//            throw new RuntimeException("注册失败");
            return ResponseVo.error(ResponseEnum.ERROR);
        }

        return ResponseVo.success();
    }

    /**
     * 登录
     *  通过输入的用户名与密码来校验数据库中是否有该用户存在
     * @param username
     * @param password
     */
    @Override
    public ResponseVo<User> login(String username, String password) {
        //最好只用用户名去查而不是用户名与密码一起输入查
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            //用户不存在,安全措施,应提示用户名或密码错误
            return ResponseVo.error(ResponseEnum.USERNAME_OR_PASSWORD_ERROR);
        }
        //验证密码的时候要忽略大小写
        //前面的是数据库的,后面的是传进来的,传进来的也用md5转化,看二者是否相同
        if (user.getPassword().equalsIgnoreCase(DigestUtils.md5DigestAsHex(
                user.getPassword().getBytes(StandardCharsets.UTF_8)))){
            //密码错误
            return ResponseVo.error(ResponseEnum.USERNAME_OR_PASSWORD_ERROR);
        }
        //不能将用户的密码也返回,在这里将用户的密码设置为空
        user.setPassword("");
        return ResponseVo.success(user);
    }

    //模拟错误的方法
    private void error() {
        throw new RuntimeException("意外错误");
    }
}
