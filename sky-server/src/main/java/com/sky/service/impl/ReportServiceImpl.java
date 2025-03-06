package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

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
            map.put("begin",beginTime); //  当天营业的时间
            map.put("end",endTime);

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
                .dateList(StringUtils.join(dateList,",")) // String类型；将集合中的数据 装好成前端需要的格式  //日期，以逗号分隔，例如：2022-10-01,2022-10-02,2022-10-03
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }
}
