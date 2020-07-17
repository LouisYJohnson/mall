package com.ibook.mall.service;

import com.github.pagehelper.PageInfo;
import com.ibook.mall.vo.OrderVo;
import com.ibook.mall.vo.ResponseVo;

public interface IOrderService {

    //创建订单
    //虽然文档中只给了传入参数shippingId,但是这里一定要加上uid,如果不用uid加以区分,不同用户之间会遇到相同的shippingId
    ResponseVo<OrderVo> create(Integer uid, Integer shippingId);

    //订单列表
    ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize);

    //订单详情
    ResponseVo<OrderVo> detail(Integer uid, Long orderNo);

    //取消订单
    ResponseVo cancel(Integer uid, Long orderNo);

    //修改订单状态
    void paid(Long orderNo);
}
