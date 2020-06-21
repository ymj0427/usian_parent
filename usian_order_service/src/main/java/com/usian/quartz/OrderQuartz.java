package com.usian.quartz;

import com.usian.mapper.LocalMessageMapper;
import com.usian.mq.MQSender;
import com.usian.pojo.LocalMessage;
import com.usian.pojo.LocalMessageExample;
import com.usian.pojo.TbOrder;
import com.usian.redis.RedisClient;
import com.usian.service.OrderService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

public class OrderQuartz implements Job {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LocalMessageMapper localMessageMapper;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private MQSender mqSender;

    /**
     * 关闭超时订单
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //1、查询超时订单
        List<TbOrder> tbOrderList = orderService.selectOverTimeTbOrder();
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //解决quartz集群任务重复执行
        if(redisClient.setnx("SETNX_LOCK_KEY:" + ip, ip, 30)) {
            //... ... ... 关闭超时订单业务

            //2、关闭超时订单
            for (int i = 0;i < tbOrderList.size();i++){
                TbOrder tbOrder = tbOrderList.get(i);
                orderService.updateOverTimeTbOrder(tbOrder);
                //3、把超时订单中的商品库存数量加回去
                orderService.updateTbItemByOrderId(tbOrder.getOrderId());
            }

            System.out.println("执行检查本地消息表的任务...." + new Date());
            LocalMessageExample localMessageExample = new LocalMessageExample();
            LocalMessageExample.Criteria criteria = localMessageExample.createCriteria();
            criteria.andStateEqualTo(0);
            List<LocalMessage> localMessageList = localMessageMapper.selectByExample(localMessageExample);
            for (LocalMessage localMessage : localMessageList) {
                mqSender.sendMsg(localMessage);
            }
            redisClient.del("SETNX_LOCK_KEY:" + ip);
        }else{
            System.out.println(
                    "============机器："+ip+" 占用分布式锁，任务正在执行=======================");
        }
    }
}
