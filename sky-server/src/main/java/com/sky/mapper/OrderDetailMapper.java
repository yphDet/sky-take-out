package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     *
     * @param orderDetails
     */
    void insertBatch(ArrayList<OrderDetail> orderDetails);

    /**
     * 根据订单号查询该订单的详细信息
     * @param ordersId
     * @return
     */
    @Select("select * from order_detail where order_id = #{ordersId}")
    List<OrderDetail> getByOrderId(Long ordersId);
}
