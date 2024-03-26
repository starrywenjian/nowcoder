package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_USER_FOLLOWEE = "followee";
    private static final String PREFIX_ENTITY_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user"; //用于缓存用户信息的

    //某个实体的赞
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //用户收到的赞
    //like:user:userId->int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //用户对某个实体进行关注
    //followee:userId:entityType->zset(entityId:now)
    public static String getUserFolloweeKey(int userId, int entityType) {
        return PREFIX_USER_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体的粉丝
    //follower:entityType:entityId->zset(userId,now)
    public static String getEntityFollowerKey(int entityType, int entityId) {
        return PREFIX_ENTITY_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户信息
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

}
