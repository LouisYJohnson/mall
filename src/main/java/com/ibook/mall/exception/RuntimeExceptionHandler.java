package com.ibook.mall.exception;

import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

//本类用来做统一拦截处理
@ControllerAdvice
public class RuntimeExceptionHandler {

    //这个注解中写要捕获的exception类
    @ExceptionHandler(RuntimeException.class)
    //想要将对象以json格式返回,加下面这个注解
    @ResponseBody
//    //将返回的http状态码改变,本课程中用不到
//    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseVo handle(RuntimeException e) {
        return ResponseVo.error(ResponseEnum.ERROR, e.getMessage());
    }

    @ExceptionHandler(UserLoginException.class)
    @ResponseBody
    public ResponseVo userLoginHandle() {
        return ResponseVo.error(ResponseEnum.NEED_LOGIN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseVo notValidExceptionHandle(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Objects.requireNonNull(bindingResult.getFieldError());
        return ResponseVo.error(ResponseEnum.PARAM_ERROR,
                bindingResult.getFieldError().getField()
                        + " " +
                        bindingResult.getFieldError().getDefaultMessage());
    }
}
