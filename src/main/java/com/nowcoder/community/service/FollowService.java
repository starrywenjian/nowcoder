package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    //关注
    public void follow(int userId, int entityType, int entityId) {
        //包含两次更新操作，需要用到事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
                String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(entityType, entityId);
                redisOperations.multi();
                redisOperations.opsForZSet().add(userFolloweeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(entityFollowerKey, userId, System.currentTimeMillis());
                return redisOperations.exec();

            }
        });
    }

    //取关
    public void unfollow(int userId, int entityType, int entityId) {
        //包含两次更新操作，需要用到事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
                String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(entityType, entityId);
                redisOperations.multi();
                redisOperations.opsForZSet().remove(userFolloweeKey, entityId);
                redisOperations.opsForZSet().remove(entityFollowerKey, userId);
                return redisOperations.exec();
            }
        });
    }

    //查看实体的粉丝数
    public long findFollowerCount(int entityType, int entityId) {
        String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(entityType, entityId);
        Long count = redisTemplate.opsForZSet().zCard(entityFollowerKey);
        return count;
    }

    //查看用户关注的实体数
    public long findFolloweeCount(int userId, int entityType) {
        String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(userFolloweeKey);
    }

    //查看用户对某实体的关注状态
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
        Double score = redisTemplate.opsForZSet().score(userFolloweeKey, entityId);
        return score != null;
    }

    //关注列表，支持分页
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(userFolloweeKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(userFolloweeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //粉丝列表，支持分页
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String userFollowerKey = RedisKeyUtil.getEntityFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(userFollowerKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(userFollowerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
