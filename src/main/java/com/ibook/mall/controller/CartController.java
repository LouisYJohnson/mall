package com.ibook.mall.controller;

import com.ibook.mall.consts.MallConst;
import com.ibook.mall.exception.RuntimeExceptionHandler;
import com.ibook.mall.form.CartAddForm;
import com.ibook.mall.form.CartUpdateForm;
import com.ibook.mall.pojo.User;
import com.ibook.mall.service.impl.CartServiceImpl;
import com.ibook.mall.vo.CartVo;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
public class CartController {

    @Autowired
    private CartServiceImpl cartService;
    //写完controller一定要记得配置拦截器

    /**
     * 注解@Valid要与BindingResult一起作为方法的传入参数,这样在方法内部就可以判断
     * 如果不加入BindingResult的话,就去统一异常处理那里处理这个异常
     * 异常名字 MethodArgumentNotValidException
     * {@link RuntimeExceptionHandler}
     * 当然,使用注解@Valid要与BindingResult的前提是在类中的参数上加入了@NotNull等注解
     * {@link CartAddForm}
     *
     * 还有其余内容见{@link UserController}
     */
    @PostMapping("/carts")
    public ResponseVo<CartVo> add(@Valid @RequestBody CartAddForm cartAddForm,
                                  HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.add(user.getId(), cartAddForm);
    }

    @GetMapping("/carts")
    public ResponseVo<CartVo> list(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.list(user.getId());
    }

    @PutMapping("/carts/{productId}")
    public ResponseVo<CartVo> update(@PathVariable Integer productId,
                            @Valid @RequestBody CartUpdateForm form,
                           HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.update(user.getId(), productId, form);
    }

    @DeleteMapping("/carts/{productId}")
    public ResponseVo<CartVo> delete(@PathVariable Integer productId,
                                     HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.delete(user.getId(), productId);
    }

    @PutMapping("/carts/selectAll")
    public ResponseVo<CartVo> selectAll(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.selectAll(user.getId());
    }

    @PutMapping("/carts/unSelectAll")
    public ResponseVo<CartVo> unSelectAll(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.unSelectAll(user.getId());
    }

    @GetMapping("/carts/products/sum")
    public ResponseVo<Integer> sum(HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        return cartService.sum(user.getId());
    }
}
