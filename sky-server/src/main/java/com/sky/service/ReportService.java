package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    /**
     * 根据时间区间统计营业额
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnover(LocalDate begin, LocalDate end);

    /**
     * 根据时间区间统计用户
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUsers(LocalDate begin, LocalDate end);

    /**
     * 统计每日订单量，有效订单量，时间区间订单总量，有效订单总量，完成率
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrders(LocalDate begin, LocalDate end);
}
