package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    //查看实体点赞数
    public long findEntityLikeCount(int entityType, int entityId) {
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(redisKey);
    }

    //进行点赞/取消赞
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        //过期版本
//        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        boolean like = redisTemplate.opsForSet().isMember(redisKey, userId);
//        if (like) {
//            redisTemplate.opsForSet().remove(redisKey, userId);
//        } else {
//            redisTemplate.opsForSet().add(redisKey, userId);
//        }
        //这里涉及到两次更新操作所以封装到了事务中进行执行
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //由于redis事务特性，查询需要在事务开始前进行
                Boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi();//开启事务

                if (isMember) {
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                } else {
                    redisOperations.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();//提交事务
            }
        });
    }

    //查询某人对某实体的点赞状态
    //0-未点，1-点了；不采用布尔作为返回值是为了拓展性，比如以后有踩，可以返回-1
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        boolean like = redisTemplate.opsForSet().isMember(redisKey, userId);
        return like ? 1 : 0;
    }

    //查询某个用户收到的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer userLikeCount = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return userLikeCount == null ? 0 : userLikeCount;
    }
}
