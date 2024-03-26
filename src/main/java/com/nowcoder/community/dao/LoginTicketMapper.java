package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
@Deprecated
public interface LoginTicketMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(LoginTicket record);

    int insertSelective(LoginTicket record);

    LoginTicket selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LoginTicket record);

    int updateByPrimaryKey(LoginTicket record);

    @Select({
            "SELECT id, user_id, ticket,status, expired FROM login_ticket ",
            "WHERE ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "UPDATE login_ticket SET status=#{status} ",
            "WHERE ticket=#{ticket}"
    })
    int updateStatus(String ticket, int status);
}