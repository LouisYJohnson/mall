package com.ibook.mall.controller;

import com.ibook.mall.consts.MallConst;
import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.exception.RuntimeExceptionHandler;
import com.ibook.mall.form.UserLoginForm;
import com.ibook.mall.form.UserRegisterForm;
import com.ibook.mall.pojo.User;
import com.ibook.mall.service.IUserService;
import com.ibook.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Objects;

/**
 * 根据文档写controller
 * https://git.imooc.com/coding-392/doc/src/master/api/用户.md
 */
//@RestController相当于同时引入了controller与restbody可以将对象以json格式返回
@RestController
@Slf4j
public class UserController {
    //完成数据校验后,注入impl来完成数据库写入的注册工作,因为接口的实现类只有这一个,所以可以这么写
    @Autowired
    private IUserService userServiceImpl;
//    //form urlencoded形式传入参数
//    @PostMapping("/register")
//    public void register(@RequestParam(value = "username") String username) {
//        log.info("username={}", username);
//
//    }

    //json形式传入参数并接收
    //User中的参数太多了,因为我们要接收的数据只有三个字段,username,password,email
    //所以新建一个form表单的UserForm去接收这些数据

    /**
     * 注解@Valid要与BindingResult一起作为方法的传入参数,这样在方法内部就可以判断
     * 如果不加入BindingResult的话,就去统一异常处理那里处理这个异常
     * 异常名字 MethodArgumentNotValidException
     * 如果加入了统一异常处理,那就没有必要加入BindingResult了,这里加进来只是为了提醒有两种写法,其实是可以去掉的
     * {@link CartController}
     * {@link RuntimeExceptionHandler}
     * 还有别的用法见
     * {@link CartController}
     */
    @PostMapping("/user/register")
    public ResponseVo register(@Valid @RequestBody UserRegisterForm userRegisterForm,
                               BindingResult bindingResult) {
        //加入BindingResult就能够判断带有@NotBlank,@NotNull,@NotEmpty的有效性了
        //如果无效,就拿出注解上的注解信息
        if (bindingResult.hasErrors()) {
            log.info("注册提交的参数有误,{} {}",
                    Objects.requireNonNull(bindingResult.getFieldError()).getField(),
                    bindingResult.getFieldError().getDefaultMessage());
            return ResponseVo.error(ResponseEnum.PARAM_ERROR, bindingResult);
        }
        //下面的代码是测试看结果用的
//        log.info("username={}", userForm.getUsername());
//        return ResponseVo.error(ResponseEnum.NEED_LOGIN);
        User user = new User();
        BeanUtils.copyProperties(userRegisterForm, user);
        //dto?
        return userServiceImpl.register(user);
    }

    //看文档,登录的时候只要传入两个参数,所以再form中再创建一个form类来接收参数
    @PostMapping("/user/login")
    public ResponseVo<User> login(@Valid @RequestBody UserLoginForm userLoginForm,
                                  BindingResult bindingResult,
                                  HttpSession session) {
        if (bindingResult.hasErrors()) {
            return ResponseVo.error(ResponseEnum.PARAM_ERROR, bindingResult);
        }

        //到这里是将登录的数据查询出来,并没有设置登录的状态
        //在login的时候就将这个user中的密码置为""了,所以这里获取到的user中密码为""
        ResponseVo<User> userResponseVo = userServiceImpl.login(userLoginForm.getUsername(), userLoginForm.getPassword());

        //设置session
        //细节:字符串型的硬编码一般不直接写在代码中,而是规定为常量
        //将这个user放入session中,设置登陆状态
        session.setAttribute(MallConst.CURRENT_USER, userResponseVo.getData());

        return userResponseVo;
    }

    //获取登录用户的信息
    //注意,这里不能写成@GetMapping("/"),就会变成访问/user/这和/user是不同的路径!
    //所以这里只能把@RequestMapping("/user")删除
    //session保存在内存里(服务器的内存,不是客户端),一般session会存到redis中去
    //改进版本:token+redis
    @GetMapping("/user")
    public ResponseVo<User> userInfo(HttpSession session) {
        //从session中获取登录状态,是否登录的判断交给拦截器去做了
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        //success将传入对象变为具有status和user类型data的ResponseVo对象
        return ResponseVo.success(user);
    }

    //TODO 判断登录状态,拦截器
    //退出登录
    /**
     * 使用@link可以直接用ctrl+鼠标左键点进去
     * {@link org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory}
     * 中的getSessionTimeoutInMinutes对最低时间做了限制,最低是1分钟,配置文件中session过期时间如果
     * 小于1分钟,则配置无效
     *
     * @param session
     * @return
     */
    @PostMapping("/user/logout")
    public ResponseVo logout(HttpSession session) {
        log.info("/user/logout sessionId={}", session.getId());
        //从session中获取登录状态
        //如果没有登陆(这点交给拦截器做了,每次发申请的时候拦截器都会运行):
        session.removeAttribute(MallConst.CURRENT_USER);
        //success将传入对象变为具有status和user类型data的ResponseVo对象
        return ResponseVo.success();
    }
}
