package com.ibook.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ibook.mall.dao.ShippingMapper;
import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.form.ShippingForm;
import com.ibook.mall.pojo.Shipping;
import com.ibook.mall.service.IShippingService;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ResponseVo<Map<String, Integer>> add(Integer uid, ShippingForm form) {
        Shipping shipping = new Shipping();
        BeanUtils.copyProperties(form, shipping);
        //写入后会返回影响的行数,成功返回1失败返回0
        int row = shippingMapper.insertSelective(shipping);
        if (row == 0) { //数据库写入失败
            return ResponseVo.error(ResponseEnum.ERROR);
        }
        //返回的对象就有一个键值对,太简单,不用单独建vo对象
        Map<String, Integer> map = new HashMap<>();
        map.put("shippingId", shipping.getId());//需要Mybatis进行配置才能拿到这个传入参数中没有的id
        return ResponseVo.success(map);
    }

    //删除要传入个人id和订单id才行,单传入一个订单id,一旦别人把不属于他的订单给改了就麻烦了
    @Override
    public ResponseVo delete(Integer uid, Integer shippingId) {
        int row = shippingMapper.deleteByIdAndUid(uid, shippingId);
        if (row == 0) {
            //删除失败
            return ResponseVo.error(ResponseEnum.DELETE_SHIPPING_FAIL);
        }
        return ResponseVo.success();
    }

    @Override
    public ResponseVo update(Integer uid, Integer shippingId, ShippingForm form) {
        Shipping shipping = new Shipping();
        BeanUtils.copyProperties(form, shipping);
        //没通过myBatis配置,就自己手写来设置对象的传入参数(非自增的数据无法通过配置xml中的方法头标签实现自动写入)
        shipping.setUserId(uid);
        shipping.setId(shippingId);
        int row = shippingMapper.updateByPrimaryKeySelective(shipping);
        if (row == 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }
        return ResponseVo.success();
    }

    @Override
    public ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippings = shippingMapper.selectByUid(uid);
        PageInfo pageInfo = new PageInfo(shippings);
        return ResponseVo.success(pageInfo);
    }
}
