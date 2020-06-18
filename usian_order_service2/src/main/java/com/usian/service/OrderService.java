package com.usian.service;

import com.usian.pojo.OrderInfo;
import com.usian.pojo.TbOrder;

import java.util.List;

public interface OrderService {

    Long insertOrder(OrderInfo orderInfo);

    //查询超时订单
    List<TbOrder> selectOverTimeTbOrder();

    //关闭超时订单
    void updateOverTimeTbOrder(TbOrder tbOrder);

    //把超时订单中的商品库存数量加回去
    void updateTbItemByOrderId(String orderId);
}
