package com.ibook.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ibook.mall.dao.OrderItemMapper;
import com.ibook.mall.dao.OrderMapper;
import com.ibook.mall.dao.ProductMapper;
import com.ibook.mall.dao.ShippingMapper;
import com.ibook.mall.enums.OrderStatusEnum;
import com.ibook.mall.enums.PaymentTypeEnum;
import com.ibook.mall.enums.ProductStatusEnum;
import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.pojo.*;
import com.ibook.mall.service.IOrderService;
import com.ibook.mall.vo.OrderItemVo;
import com.ibook.mall.vo.OrderVo;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional  //加入这个注解就可以在两个表写入时一个表出现异常就两者回滚,必须要数据库引擎支持事务才行
    public ResponseVo<OrderVo> create(Integer uid, Integer shippingId) {
        //校验收货地址(总之要查出来)
        Shipping shipping = shippingMapper.selectByUidAndShippingId(uid, shippingId);
        if (shipping == null) {
            return ResponseVo.error(ResponseEnum.SHIPPING_NOT_EXIST);
        }

        //通过uid获取购物车,校验是否有选中的以及选中商品库存(因为后面都是只校验选中的商品,所以在这里筛选出来状态为选中的商品)
        List<Cart> cartList = cartService.listForCart(uid).stream()
                .filter(Cart::getProductSelected)
                .collect(Collectors.toList());
        //如果没有选中的商品,错误
        if (CollectionUtils.isEmpty(cartList)) {
            return ResponseVo.error(ResponseEnum.CART_SELECTED_IS_EMPTY);
        }
        //TODO 使用mysql中的in语句替代for循环将所有数据一次性查出
        //接下来检验是否有商品以及库存
        //获取cartList里的productIds(所有productId)
        Set<Integer> productIdSet = cartList.stream().map(Cart::getProductId).collect(Collectors.toSet());
        //由所有id与mysql中的in语句获取所有的商品
        List<Product> productsList = productMapper.selectByProductIdSet(productIdSet);
        //将productList变成下面这种形式的map,就可以直接通过购物车中的商品id访问到product对象,不需要第二次遍历
        Map<Integer, Product> map = productsList.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        //使用购物车中的商品id去product表中去查,如果product中没有数据(没有该商品或者库存不够),
        // 说明加车的商品没有办法购买,要报错
        //购物车中的所有商品构成一个list
        List<OrderItem> orderItemList = new ArrayList<>();
        //生成订单号
        Long orderNo = generateOrderNo();
        for (Cart cart : cartList) {
            Product product = map.get(cart.getProductId());
            //是否有商品
            if (product == null) {
                //购物车中的商品在数据库中不存在
                return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST,
                        "商品不存在. productId = " + cart.getProductId());
            }
            //商品的上下架状态
            if (!ProductStatusEnum.ON_SALE.getCode().equals(product.getStatus())) {
                return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE,
                        "商品不是在售状态. " + product.getName());
            }
            //库存是否充足
            if (product.getStock() < cart.getQuantity()) {
                return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR,
                        "库存不正确. " + product.getName());
            }
            //减库存,如果库存充足,才减
            product.setStock(product.getStock() - cart.getQuantity());
            int row = productMapper.updateByPrimaryKeySelective(product);
            if (row <= 0) {
                return ResponseVo.error(ResponseEnum.ERROR);
            }
            //计算总价格,只计算被选中的商品
            OrderItem orderItem = buildOrderItem(uid, orderNo, cart.getQuantity(), product);
            orderItemList.add(orderItem);
        }
        //生成订单,入库:order与order_item表(如果不加入事务任何一个表写入失败都会影响整体数据)
        Order order = buildOrder(uid, orderNo, shippingId, orderItemList);
        //写入order订单数据库
        int rowForOrder = orderMapper.insertSelective(order);
        //写入失败
        if (rowForOrder <= 0) {
            ResponseVo.error(ResponseEnum.ERROR);
        }
        //写入orderItem订单商品列表数据库
        int rowForOrderItem = orderItemMapper.batchInsert(orderItemList);
        //入库失败
        if (rowForOrderItem <= 0) {
            ResponseVo.error(ResponseEnum.ERROR);
        }

        //更新购物车(删掉选中商品)
        //Redis有事务(打包命令),不能回滚
        for (Cart cart : cartList) {
            cartService.delete(uid, cart.getProductId());
        }

        //构造orderVO对象,返回给前端
        buildOrderVo(order, orderItemList, shipping);
        return null;
    }

    @Override
    public ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        //得到当前uid对应的所有订单
        List<Order> orderList = orderMapper.selectByUid(uid);

        Set<Long> collectNoSet = orderList.stream()
                .map(Order::getOrderNo)
                .collect(Collectors.toSet());
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoSet(collectNoSet);

        Set<Integer> shippingIdSet = orderList.stream()
                .map(Order::getShippingId)
                .collect(Collectors.toSet());
        List<Shipping> shippingList = shippingMapper.selectByIdSet(shippingIdSet);

        //将数据转为一个map结构,key为订单id,value为订单id对应的商品List
        Map<Long, List<OrderItem>> orderItemMap = orderItemList.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderNo));
        //将数据转为一个map结构,key为收货地址的id,value为收货地址
        Map<Integer, Shipping> shippingMap = shippingList.stream()
                .collect(Collectors.toMap(Shipping::getId, shipping -> shipping));
        List<OrderVo> orderVoList = new ArrayList<>();
        for (Order order : orderList) {
            OrderVo orderVo = buildOrderVo(order,
                    orderItemMap.get(order.getOrderNo()),
                    shippingMap.get(order.getShippingId()));
            orderVoList.add(orderVo);
        }

        PageInfo pageInfo = new PageInfo<>(orderList);
        //根据文档,pageInfo中的List中装的是OrderVo
        pageInfo.setList(orderVoList);
        return ResponseVo.success(pageInfo);
    }

    @Override
    public ResponseVo<OrderVo> detail(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        //判断订单是否存在,如果存在,判断这个订单是否属于这个人
        if (order == null || !order.getUserId().equals(uid)) {
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXIST);
        }
        //如果通过校验,把orderItem都查出来
        Set<Long> orderNoSet = new HashSet<Long>();
        orderNoSet.add(order.getOrderNo());
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoSet(orderNoSet);
        //查地址
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        OrderVo orderVo = buildOrderVo(order, orderItemList, shipping);
        return ResponseVo.success(orderVo);
    }

    @Override
    public ResponseVo cancel(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        //判断订单是否存在,如果存在,判断这个订单是否属于这个人
        if (order == null || !order.getUserId().equals(uid)) {
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXIST);
        }
        //只有未付款订单可以取消,看公司业务如何规定
        if (!order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())) {
            return ResponseVo.error(ResponseEnum.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderStatusEnum.CANCELED.getCode());
        order.setCloseTime(new Date());
        //更新订单信息
        int row = orderMapper.updateByPrimaryKeySelective(order);
        if (row <= 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }
        return ResponseVo.success();
    }

    @Override
    public void paid(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        //判断订单是否存在,如果存在,判断这个订单是否属于这个人
        if (order == null) {
            throw new RuntimeException(ResponseEnum.ORDER_NOT_EXIST.getDesc() + "订单id" + orderNo);
        }
        //只有未付款订单可以变成已付款,看公司业务如何规定
        if (!order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())) {
            throw new RuntimeException(ResponseEnum.ORDER_STATUS_ERROR.getDesc() + "订单id" + orderNo);
        }

        order.setStatus(OrderStatusEnum.PAID.getCode());
        order.setPaymentTime(new Date());
        //更新订单信息
        int row = orderMapper.updateByPrimaryKeySelective(order);
        if (row <= 0) {
            throw new RuntimeException("将订单更新为已支付状态失败,订单id " + orderNo);
        }
    }

    private OrderVo buildOrderVo(Order order, List<OrderItem> orderItemList, Shipping shipping) {
        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(order, orderVo);

        List<OrderItemVo> orderItemVoList = orderItemList.stream().map(e -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            BeanUtils.copyProperties(e, orderItemVo);
            return orderItemVo;
        }).collect(Collectors.toList());
        orderVo.setOrderItemVoList(orderItemVoList);
        if (shipping != null) {
            orderVo.setShippingId(shipping.getId());
            orderVo.setShippingVo(shipping);
        }

        return orderVo;
    }

    private Order buildOrder(Integer uid,
                             Long orderNo,
                             Integer shippingId,
                             List<OrderItem> orderItemList) {
        BigDecimal payment = orderItemList.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(uid);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        //支付方式,1-在线支付
        order.setPaymentType(PaymentTypeEnum.PAY_ONLINE.getCode());
        //课程不涉及运费,所以写死为0
        order.setPostage(0);
        order.setStatus(OrderStatusEnum.NO_PAY.getCode());
        return order;
    }

    //此方法用来生成订单号
    //这里使用简单的方法,在企业中使用分布式唯一id/主键
    private Long generateOrderNo() {
        return System.currentTimeMillis() + new Random().nextInt(999);
    }

    private OrderItem buildOrderItem(Integer uid, Long orderNo, Integer quantity, Product product) {
        OrderItem item = new OrderItem();
        item.setUserId(uid);
        item.setOrderNo(orderNo);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getMainImage());
        item.setCurrentUnitPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}
