package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;


    /**
     *
     * @param ordersSubmitDTO
     * @return
     */
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //  1、判断前用户的购物车是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
            //  1.1：查询当前用户的购物车数据
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);    // 购物车中是否有商品
            //  1.2；对用户购物车数据进行判断
        if (shoppingCartList == null || shoppingCartList.size() == 0){
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //  2、获取用户的配送的地址
        /**
         *
         */
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }


        //  3、构造订单数据
        Orders orders = new Orders();   //  关联 对应的 数据库
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));   //  订单号
        orders.setUserId(userId);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);

        //  4、向订单表中插入数据
        orderMapper.insert(orders);

        //  5、订单明细数据
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }


        //  向明细表插入n条数据  为什么？？？
        orderDetailMapper.insertBatch(orderDetailList);     //  难道用于展示吗？

        //  清空购物车信息
        shoppingCartMapper.deleteByUserId(userId);

        //  封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();


        return orderSubmitVO;
    }
}
