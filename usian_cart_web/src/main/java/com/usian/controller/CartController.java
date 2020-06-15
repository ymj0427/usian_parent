package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.ItemServiceFeignClient;
import com.usian.pojo.TbItem;
import com.usian.utils.CookieUtils;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/frontend/cart")
public class CartController {

    @Value("${CART_COOKIE_KEY}")
    private String CART_COOKIE_KEY;

    @Value("${CART_COOKIE_EXPIRE}")
    private Integer CART_COOKIE_EXPIRE;

    @Autowired
    private CartServiceFeign cartServiceFeign;

    @Autowired
    private ItemServiceFeignClient itemServiceFeignClient;

    /**
     * 将商品加入购物车
     */
    @RequestMapping("/addItem")
    public Result addItem(Long itemId, String userId, @RequestParam(defaultValue = "1")Integer num,
                          HttpServletRequest request,
                          HttpServletResponse response){
        if(StringUtils.isBlank(userId)){
            //*********在用户未登录的状态下**********
            //1、从cookie中查询商品列表
            Map<String, TbItem> cart = getCartFromCookie(request);
            //2、添加商品到购物车中
            addItemToCart(cart,itemId,num);
            //3、把购物车商品列表写入cookie
            addClientCookie(request,response,cart);
            return Result.ok();
        }else {
            /***********在用户已登录的状态**********/
            // 1、从redis中查询商品列表。
            Map<String,TbItem> cart = getCartFromRedis(userId);
            //2、将商品添加大购物车中
            this.addItemToCart(cart,itemId,num);
            //3、将购物车缓存到 redis 中
            Boolean addCartToRedis = this.addCartToRedis(userId, cart);
            if(addCartToRedis){
                return Result.ok();
            }
            return Result.error("error");
        }
    }

    /**
     * 把购车商品列表写入redis
     * @param userId
     * @param cart
     */
    private Boolean addCartToRedis(String userId, Map<String, TbItem> cart) {
        return cartServiceFeign.insertCart(userId, cart);
    }

    /**
     * 从redis中查询购物车
     * @param userId
     */
    private Map<String, TbItem> getCartFromRedis(String userId) {
        Map<String,TbItem> cart = cartServiceFeign.selectCartByUserId(userId);
        if (cart != null && cart.size() >0){
            return cart;
        }
        return new HashMap<String, TbItem>();
    }

    /**
     * 把购车商品列表写入cookie
     * @param request
     * @param response
     * @param cart
     */
    private void addClientCookie(HttpServletRequest request, HttpServletResponse response, Map<String, TbItem> cart) {
        String cartJson = JsonUtils.objectToJson(cart);
        CookieUtils.setCookie(request,response,this.CART_COOKIE_KEY,cartJson,CART_COOKIE_EXPIRE,true);
    }

    /**
     * 将商品添加到购物车中
     * @param cart
     * @param itemId
     * @param num
     */
    private void addItemToCart(Map<String, TbItem> cart, Long itemId, Integer num) {
        //从购物车中获取商品
        TbItem tbItem = cart.get(itemId.toString());
        if (tbItem != null){
            //商品列表中存在该商品，商品数量相加
            tbItem.setNum(tbItem.getNum()+num);
        }else{
            //商品列表中不存在该商品，根据id查询商品，并添加到购车列表
             tbItem = itemServiceFeignClient.selectItemInfo(itemId);
             tbItem.setNum(num);
        }
        cart.put(itemId.toString(),tbItem);
    }

    /**
     * 获取购物车
     * @param request
     * @return
     */
    private Map<String, TbItem> getCartFromCookie(HttpServletRequest request) {
        String cookieValue = CookieUtils.getCookieValue(request, this.CART_COOKIE_KEY, true);
        if (StringUtils.isNotBlank(cookieValue)){
            //购物车已存在
            Map<String,TbItem> map = JsonUtils.jsonToMap(cookieValue, TbItem.class);
            return map;
        }
        //购物车不存在
        return new HashMap<String, TbItem>() ;
    }

    /**
     * 查看购物车
     */
    @RequestMapping("/showCart")
    public Result showCart(String userId, HttpServletRequest
            request, HttpServletResponse response){
        try {
            if (StringUtils.isBlank(userId)){
                //不在登录状态
                List<TbItem> list = new ArrayList<>();
                Map<String, TbItem> cartCookie = this.getCartFromCookie(request);
                Set<String> keySet = cartCookie.keySet();
                for (String s : keySet) {
                    list.add(cartCookie.get(s));
                }
                return Result.ok(list);
            }else {
                //在登陆状态
                List<TbItem> list = new ArrayList<>();
                Map<String, TbItem> cart = this.getCartFromRedis(userId);
                Set<String> keySet = cart.keySet();
                for (String s : keySet) {
                    list.add(cart.get(s));
                }
                return Result.ok(list);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.error("error");
    }

    /**
     * 修改购物车
     */
    @RequestMapping("/updateItemNum")
    public Result updateItemNum(String userId,Long itemId,Integer num,
                                HttpServletRequest request,HttpServletResponse response){
        try {
            if (StringUtils.isBlank(userId)){
                //未登录状态
                //从cookie中获取购物车列表
                Map<String, TbItem> cart = getCartFromCookie(request);

                //修改购物车的商品
                TbItem tbItem = cart.get(itemId.toString());
                tbItem.setNum(num);
                cart.put(itemId.toString(),tbItem);
                //把商品写入cookie
                addClientCookie(request,response,cart);
            }else {
                //已登录
                Map<String,TbItem> cart = getCartFromRedis(userId);
                TbItem item = cart.get(itemId.toString());
                if(item != null){
                    item.setNum(num);
                }
                //将新的购物车缓存到 Redis 中
                this.addCartToRedis(userId,cart);
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
        }
        return Result.error("修改错误");
    }

    /**
     * 删除购物车中的商品
     */
    @RequestMapping("/deleteItemFromCart")
    public Result deleteItemFromCart(Long itemId, String userId, HttpServletRequest
            request, HttpServletResponse response) {
        try {
            if (StringUtils.isBlank(userId)) {
                //在用户未登录的状态下
                Map<String,TbItem> cart = this.getCartFromCookie(request);
                cart.remove(itemId.toString());
                this.addClientCookie(request,response,cart);

            } else {
                // 在用户已登录的状态
                Map<String,TbItem> cart = this.getCartFromRedis(userId);
                cart.remove(itemId.toString());
                //将新的购物车缓存到 Redis 中
                this.addCartToRedis(userId,cart);
            }
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error("删除失败");
    }


}
