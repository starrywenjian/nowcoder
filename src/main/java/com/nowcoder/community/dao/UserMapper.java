package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

//    @Select("select * from user where user_id = #{id}")
//    User selectById(Integer userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    @Select({"SELECT * FROM  user WHERE id = #{id}"})
    @ResultMap("BaseResultMap")
    User selectById(int id);

    @Select("SELECT * FROM user WHERE username=#{username}")
    @ResultMap("BaseResultMap")
    User selectByUsername(String username);

    User selectByEmail(String email);

}