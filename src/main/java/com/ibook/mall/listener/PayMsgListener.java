package com.ibook.mall.listener;

import com.google.gson.Gson;
import com.ibook.mall.pojo.PayInfo;
import com.ibook.mall.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "payNotify")
@Slf4j
public class PayMsgListener {

    @Autowired
    private OrderServiceImpl orderService;

    //带有这个注解的方法去接收监听到的消息
    @RabbitHandler
    public void process(String msg) {
        log.info("[接收到消息] => {}", msg);
        //接收到的是json字符串,转回对象
        //PayInfo的正确姿势:pay项目提供一个client(jar包),mall项目引用jar包,而不是在这个项目中新建
        PayInfo payInfo = new Gson().fromJson(msg, PayInfo.class);

        if (payInfo.getPlatformStatus().equals("SUCCESS")) {
            //修改订单里的状态
            orderService.paid(payInfo.getOrderNo());
        }
    }
}
