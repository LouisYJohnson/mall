package com.ibook.mall;

import com.ibook.mall.consts.MallConst;
import com.ibook.mall.exception.UserLoginException;
import com.ibook.mall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link InterceptorConfig}
 */
//拦截器的配置在InterceptorConfig中
//在这里写是没有实际用到的,必须在config中将这个类配置进去才行
@Slf4j
public class UserLoginInterceptor implements HandlerInterceptor {
    /**
     * 在请求前进行拦截
     *
     * @param request
     * @param response
     * @param handler
     * @return return的true和false的含义:
     * true表示继续流程
     * false表示中断
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle...");
        request.getSession();
        User user = (User) request.getSession().getAttribute(MallConst.CURRENT_USER);

        //如果出错,是要返回内容的,但是方法返回的是布尔类型,怎么办?
        //之前用到了统一异常处理所以我们在这里直接抛出对应异常即可(这里抛出的是子类)
        //那个异常的处理已经对应了返回一个符合前端要求的数据格式了
        /**
         * {@link UserLoginException}
         * {@link com.ibook.mall.exception.RuntimeExceptionHandler}
         */
        if (user == null) {
            log.info("user == null");
            throw new UserLoginException();
//            return false;
//            return ResponseVo.error(ResponseEnum.NEED_LOGIN);
        }
        return true;
    }
}
