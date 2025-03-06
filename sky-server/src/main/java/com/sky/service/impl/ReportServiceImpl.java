package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据时间区间统计营业额
     *
     * @param begin
     * @param end
     * @return
     */
    /*      根据时间区间统计营业额
        1、营业额如何统计
            1.1 确定确定每天的营业额；
            1.2 营业额是已完成订单的统计
            1.3 确定时间
     */
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);  //  日期计算，获得指定日期后1天的日期
            dateList.add(begin);
        }


        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //  确定每天的营业时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //  构建查询营业额条件
            HashMap map = new HashMap();
            map.put("status", Orders.COMPLETED);    //  完成订单
            map.put("begin", beginTime); //  当天营业的时间
            map.put("end", endTime);

            /*
                select sum(amount) from orders where order_time > ? and order_time < ? and status = 5;
             */
            //  统计营业额
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }


        //  数据封装返回给前端
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ",")) // String类型；将集合中的数据 装好成前端需要的格式  //日期，以逗号分隔，例如：2022-10-01,2022-10-02,2022-10-03
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }


    public OrderReportVO getOrders(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();

        if (begin != end) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        ArrayList<Integer> orderCountList = new ArrayList<>();
        ArrayList<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //  每日订单数，每日完成订单数
            Integer orderCount = orderMapper.getByTime(null, beginTime, endTime);
            Integer validOrderCount = orderMapper.getByTime(Orders.COMPLETED, beginTime, endTime);

            orderCount = orderCount == null ? 0 : orderCount;
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //时间区间内的总订单数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 时间区间内的总有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0D;
        if (totalOrderCount != 0) {

            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUsers(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();

        if (begin != end) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        ArrayList<Integer> newUserList = new ArrayList<>();
        ArrayList<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //  查询当天新增用户,select count(id) from user where create_time > begin and create_time < end;
            Integer newUsers = userMapper.sumUsers(beginTime, endTime);
            newUsers = newUsers == null ? 0 : newUsers;
            newUserList.add(newUsers);

            //  统计当天总用户数量    select count(id) from user where create_time < end;
            Integer users = userMapper.sumUsers(null, endTime);
            users = users == null ? 0 : users;
            totalUserList.add(users);

        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    public SalesTop10ReportVO getTop(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime,endTime);

        List<String> nameList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }
}
