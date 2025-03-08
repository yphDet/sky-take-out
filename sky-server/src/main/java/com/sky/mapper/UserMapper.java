package com.sky.mapper;

import com.sky.entity.Orders;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     *  根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     *
     * @param user
     */
    void insert(User user);

    @Insert("select * from user where id = #{userId}")
    User getById(Long userId);


    /**
     * 统计用户数量
     * @param beginTime
     * @param endTime
     * @return
     */
    Integer sumUsers(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     *
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
