package com.usian.feign;


import com.usian.pojo.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("usian-order-service")
public interface OrderServiceFeign {

    @RequestMapping("/service/order/insertOrder")
    public Long insertOrder(OrderInfo orderInfo);

}
