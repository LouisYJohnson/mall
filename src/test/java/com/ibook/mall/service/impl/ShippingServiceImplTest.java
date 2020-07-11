package com.ibook.mall.service.impl;

import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.form.ShippingForm;
import com.ibook.mall.vo.ResponseVo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ShippingServiceImplTest {

    @Autowired
    private ShippingServiceImpl shippingService;

    private Integer uid = 1;

    @Test
    public void add() {
        ShippingForm shippingForm = new ShippingForm();
        shippingForm.setReceiverName("林义钧");
        shippingForm.setReceiverAddress("中国");
        shippingForm.setReceiverCity("大连");
        shippingForm.setReceiverMobile("123456789");
        shippingForm.setReceiverPhone("188888888");
        shippingForm.setReceiverDistrict("旅顺");
        shippingForm.setReceiverZip("000000");
        ResponseVo<Map<String, Integer>> responseVo = shippingService.add(uid, shippingForm);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(), responseVo.getStatus());
    }

    @Test
    public void delete() {
    }

    @Test
    public void update() {
    }

    @Test
    public void list() {
    }
}