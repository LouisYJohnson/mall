package com.ibook.mall.service.impl;

import com.google.gson.Gson;
import com.ibook.mall.dao.ProductMapper;
import com.ibook.mall.enums.ProductStatusEnum;
import com.ibook.mall.enums.ResponseEnum;
import com.ibook.mall.form.CartAddForm;
import com.ibook.mall.form.CartUpdateForm;
import com.ibook.mall.pojo.Cart;
import com.ibook.mall.pojo.Product;
import com.ibook.mall.service.ICartService;
import com.ibook.mall.vo.CartProductVo;
import com.ibook.mall.vo.CartVo;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements ICartService {

    private static final String CART_REDIS_KEY_TEMPLATE = "cart_%d";

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Gson gson = new Gson();

    //将商品加入购物车
    @Override
    public ResponseVo<CartVo> add(Integer uid, CartAddForm form) {
        Integer quantity = 1;

        Product product = productMapper.selectByPrimaryKey(form.getProductId());

        //首先判断商品是否存在
        if (product == null) {
            return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST);
        }
        //商品是否是正常的在售状态
        if (!product.getStatus().equals(ProductStatusEnum.ON_SALE.getCode())) {
            return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE);
        }
        //商品库存是否充足
        if (product.getStock() <= 0) {
            return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR);
        }
        //上面的校验都通过了,说明这次可以加购物车,就可以将数据写入redis中去了(引入依赖+yml文件中做配置)
        //set(或者put,表示向redis中设置值的时候所用到的方法)中有3个参数,一个是key一个是value还有一个是过期时间
        //前面的opsForXxx表示key的类型
        //这里按照开发文档:(前面是cart,后面的1是uid),所以定义常量,使用String.format()方法将传入的字符串加入内容
        //  key: cart_1
        //购物车中存的数据最好是一个类似于书签一样的东西然后用这个书签去数据库中查找,
        // 如果不这么做,几天后回来看购物车如果此时商品价格更新了,但是购物车中的价格是不会更新的
        //所以存入数据库的最好只有productId,quantity,productSelected这几个字段
        //将这几个字段放入一个对象传入即可
        //但是Redis中接收的是String类型的对象,所以要将对象转换成json格式的字符串才能传入value
        //使用组件json引入(json序列化)
        //使用hash类型的key来存储,有三个参数,第一个参数就是key,第二个参数是hashMap中的key,第三个参数是HashMap中的value

        //业务逻辑:在加入购物车的时候要先从redis中把对应商品在购物车中的信息取出来,然后每次放购物车都+1才对
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        //查到的是一个json字符串,想要把json字符串变成对象,使用Gson中的反序列化即可
        Cart cart;
        String value = opsForHash.get(redisKey, String.valueOf(product.getId()));
        if (StringUtils.isEmpty(value)) {
            //没有该商品,新增
            cart = new Cart(product.getId(), quantity, form.getSelected());
        } else {
            //已经有了,数量+1再放回redis中
            //将查询到的json字符串反序列化成对象
            cart = gson.fromJson(value, Cart.class);
            cart.setQuantity(cart.getQuantity() + quantity);
        }

        //第一个参数用户id,第二个参数商品id,第三个参数,商品id对应的商品详情对象
        opsForHash.put(String.format(CART_REDIS_KEY_TEMPLATE, uid),
                String.valueOf(product.getId()),
                gson.toJson(cart));
        return list(uid);
    }

    //uid为用户id,就是根据用户id去redis查询属于这个用户的购物车
    @Override
    public ResponseVo<CartVo> list(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        //拿到所有购物车中的商品的id以及数量等信息,将这些信息中的一部分作为依据去数据库中查询商品的详细信息
        //并将查询到的详细信和当前map中的数据进行整合,返回一个CartVo对象,就是返回给前端的json数据
        Map<String, String> entries = opsForHash.entries(redisKey);

        //所有的都选了才叫全选,所以在for循环中只要碰到一个没选,就把状态设置为false
        boolean selectAll = true;
        Integer cartTotalQuantity = 0;
        BigDecimal cartTotalPrice = BigDecimal.ZERO;

        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = new ArrayList<>();

        //遍历所有购物车中的数据,并去数据库中查询商品的详细信息
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Integer productId = Integer.valueOf(entry.getKey());
            Cart cart = gson.fromJson(entry.getValue(), Cart.class);

            //TODO 需要优化,使用mysql中的in 来将所有数据查出来
            Product product = productMapper.selectByPrimaryKey(productId);
            if (product != null) {
                CartProductVo cartProductVo = new CartProductVo(productId,
                        cart.getQuantity(),
                        product.getName(),
                        product.getSubtitle(),
                        product.getMainImage(),
                        product.getPrice(),
                        product.getStatus(),
                        product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())),
                        product.getStock(),
                        cart.getProductSelected()
                );
                cartProductVoList.add(cartProductVo);
                //有一个没有选中,就是没有全选
                if (!cart.getProductSelected()) {
                    selectAll = false;
                }
                //计算总价(只计算选中的)
                if (cart.getProductSelected()) {
                    cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
                }
            }
            //购物车中商品总数,是直接从redis中拿到的
            cartTotalQuantity += cart.getQuantity();
        }
        //将之前得到的商品详情list加入到对象的List中
        cartVo.setCartProductVoList(cartProductVoList);
        //有一个没有选中就不叫全选
        cartVo.setSelectedAll(selectAll);
        //计算购物车中的总数
        cartVo.setCartTotalQuantity(cartTotalQuantity);
        //购物车的总价
        cartVo.setCartTotalPrice(cartTotalPrice);
        return ResponseVo.success(cartVo);
    }

    @Override
    public ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm form) {
        //要更新内容,首先要查出来
        //业务逻辑:在加入购物车的时候要先从redis中把对应商品在购物车中的信息取出来,然后每次放购物车都+1才对
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        //查到的是一个json字符串,想要把json字符串变成对象,使用Gson中的反序列化即可
        String value = opsForHash.get(redisKey, String.valueOf(productId));
        if (StringUtils.isEmpty(value)) {
            //没有该商品,说明数据有问题,报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }
        //已经有了,修改内容并放回redis中
        Cart cart = gson.fromJson(value, Cart.class);
        //分别判断两个非必传参数,并更新数据
        if (form.getQuantity() != null && form.getQuantity() >= 0) {
            cart.setQuantity(form.getQuantity());
        }
        if (form.getSelected() != null) {
            cart.setProductSelected(form.getSelected());
        }
        //更新数据
        opsForHash.put(redisKey, String.valueOf(productId), gson.toJson(cart));
        //根据uid查询更新好的数据并将其返回(因为list方法返回的数据格式和接口中的所有json数据格式都是一样的)
        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> delete(Integer uid, Integer productId) {

        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        //查到的是一个json字符串,想要把json字符串变成对象,使用Gson中的反序列化即可
        String value = opsForHash.get(redisKey, String.valueOf(productId));
        if (StringUtils.isEmpty(value)) {
            //没有该商品,说明数据有问题,报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }
        //已经有了,将其从redis中删除
        opsForHash.delete(redisKey, String.valueOf(productId));
        //根据uid查询更新好的数据并将其返回(因为list方法返回的数据格式和接口中的所有json数据格式都是一样的)
        return list(uid);
    }

    //全选就是遍历一下用户id(uid)对应的购物车然后将里面的对象选中都设置为true
    @Override
    public ResponseVo<CartVo> selectAll(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        //拿到所有购物车中的商品的id以及数量等信息
        Map<String, String> entries = opsForHash.entries(redisKey);

        for (Cart cart : listForCart(uid)) {
            cart.setProductSelected(true);
            opsForHash.put(redisKey,
                    String.valueOf(cart.getProductId()),
                    gson.toJson(cart));
        }

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> unSelectAll(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        //拿到所有购物车中的商品的id以及数量等信息
        Map<String, String> entries = opsForHash.entries(redisKey);

        //这里传入的是Cart而不是CartVo是因为redis中只存了Cart对应的json数据,通过该数据能够通过list方法查到详细信息
        for (Cart cart : listForCart(uid)) {
            cart.setProductSelected(false);
            opsForHash.put(redisKey,
                    String.valueOf(cart.getProductId()),
                    gson.toJson(cart));
        }
        return list(uid);
    }

    @Override
    public ResponseVo<Integer> sum(Integer uid) {
        Integer sum = listForCart(uid).stream().map(Cart::getQuantity).reduce(0, Integer::sum);
        return ResponseVo.success(sum);
    }

    //将uid对应的购物车中所有的商品索引拿出来,并组成一个List返回,通过这个list可以去数据库中找到对应的商品细节
    private List<Cart> listForCart(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        Map<String, String> entries = opsForHash.entries(redisKey);

        List<Cart> cartList = new ArrayList<>();
        //拿到所有购物车中的商品索引信息
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            cartList.add(gson.fromJson(entry.getValue(), Cart.class));
        }
        return cartList;
    }


}
