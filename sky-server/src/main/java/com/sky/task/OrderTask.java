package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     *  取消用户端支付超时订单
     */
    @Scheduled(cron = "0 * * * * ? ")   //  每隔一分钟执行一次
    public void processTimeoutOrder(){
        log.info("开始进行支付超时订单处理：{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(15);

        //  查询订单表中的超时订单
        //  select * from orders where status = ? && create_time < (当前时间 - 15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.PENDING_PAYMENT,time);

        if(ordersList != null && ordersList.size() > 0){
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            });
        }
    }

    /**
     * 处理商家端
     */
    @Scheduled(cron = "0 0 * * * ? ")
    public void processDeliveryOrder(){ //  每个一个小时执行一次
        log.info("处理派送中订单：{}",new Date());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if(ordersList != null && ordersList.size() > 0){
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            });
        }
    }

}
