package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface OrderDetailMapper {
    /**
     *
     * @param orderDetails
     */
    void insertBatch(ArrayList<OrderDetail> orderDetails);
}
