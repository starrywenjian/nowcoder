package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Message record);

    int insertSelective(Message record);

    Message selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKeyWithBLOBs(Message record);

    int updateByPrimaryKey(Message record);


    //查看会话列表(分页)
    List<Message> selectConversations(int userId, int offset, int limit);

    //查看会话数量
    int selectConvesrsationCount(int userId);

    //查看具体会话的私信列表(分页)
    List<Message> selectLetters(String conversationId,int offset, int limit);
    //查看私信数量
    int selectLetterCount(String conversationId);

    //查看未读消息,会话id作为可选条件，放进if判断中
    int selectLetterUnreadCount(int userId, String conversationId);

    //更新消息状态
    int updateStatus(List<Integer> ids, int status);

    //最新的一条系统消息（评论/点赞/关注）
    Message selectLatestNotice(int userId, String topic);
    //某类系统消息的总条数
    int selectNoticeCount(int userId, String topic);
    //系统消息的未读数
    int selectUnreadNoticeCount(int userId, String topic);

    //某类消息列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

    //将未读消息设为已读消息
    int updateNoticeStatus(int userId, String topic);

}