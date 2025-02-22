package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，统一为公共字段赋值
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 通知自动填充公共字段
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("公共字段自动填充...");
        //  获取方法签名对象
        MethodSignature signature = (MethodSignature)joinPoint.getSignature(); // signature 签名
        //  获取方法上的注解
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //  获取注解中的操作类型
        OperationType operationType = autoFill.value();

        //  获取当前目标方法的参数
        Object[] args = joinPoint.getArgs();

        if(args == null || args.length == 0) {
            return;
        }

        //  实体对象    【同意：参数列表中的首位】
        Object entity = args[0];
        //  准备赋值的数据
        LocalDateTime time = LocalDateTime.now();
        Long empId = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT){
            //  当前执行的时insert操作，为4个字段赋值
            try {
                //  获取set方法对象----Method
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

//                try {
                    setCreateTime.invoke(entity,time);
                    setUpdateTime.invoke(entity,time);
                    setCreateUser.invoke(entity,empId);
                    setUpdateUser.invoke(entity,empId);
//                } catch (IllegalAccessException e) {
//                    throw new RuntimeException(e);
//                } catch (InvocationTargetException e) {
//                    throw new RuntimeException(e);
//                }

            } catch (/*NoSuchMethodException e*/    Exception ex) {
                log.error("公共字段自动填充失败：{}",ex.getMessage());
            }
        }else {
            try {
                //  获取set方法对象----Method
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //  通过发射调用目标对象的方法
                setUpdateTime.invoke(entity,time);
                setUpdateUser.invoke(entity,empId);
            } catch (Exception ex) {
                log.error("公共字段自动填充失败：{}",ex.getMessage());
            }
        }
    }
}
