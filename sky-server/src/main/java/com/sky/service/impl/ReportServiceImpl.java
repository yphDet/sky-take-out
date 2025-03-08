package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService    workspaceService;

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

    /**
     * 导出30天的数据报表
     * @param httpServletResponse
     */
    public void exportBusinessDate(HttpServletResponse httpServletResponse) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        //  设计excel模板文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);

            XSSFSheet sheet = excel.getSheet("sheet1");

            sheet.getRow(1).getCell(0).setCellValue(begin + "至" + end);

            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);

                //
                businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

                row = sheet.getRow(7 + 1);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());

            }

            //通过数据流将文件下载到客户端浏览器中
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            excel.write(outputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
