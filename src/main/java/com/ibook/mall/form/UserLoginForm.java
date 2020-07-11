package com.ibook.mall.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserLoginForm {

    //@NotBlank//不能是空白,用于字符String,会判断空格,如果就传一个空格过来,是非法的
    //@NotNull//不能是null
    ///@NotEmpty 用于集合,检查数组或者集合里面是不是空的
    //后面加message后如果出错,就能通过bindingResult.getFieldError().getDefaultMessage()
    //方法拿到message中的值(注意在传入参数的时候还要加入注解@Valid)
//    @NotBlank(message = "用户名不能为空")
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
