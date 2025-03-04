package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class MyTask {

    @Scheduled(cron = "1-2 * * * * ?")  //  每一分钟内的1 - 2秒时执行依次
    public void excuteTask(){
        log.info("定时任务测试：{}",new Date());
    }
}
