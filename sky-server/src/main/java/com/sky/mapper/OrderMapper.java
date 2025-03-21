package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 获取订单表中的超时订单
     * @param pendingPayment
     * @param time
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time = #{time}")
    List<Orders> getByStatusAndOrdertimeLT(Integer pendingPayment, LocalDateTime time);

    /**
     *  分页查询订单历史
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单号查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select count(*) from orders where status = #{status}")
    Integer countStatus(Integer deliveryInProgress);

    /**
     * 根据时间区间统计营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     *
     * @param status
     * @param begin
     * @param end
     * @return
     */
    Integer getByTime(Integer status, LocalDateTime begin, LocalDateTime end);

    /**
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     *
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
