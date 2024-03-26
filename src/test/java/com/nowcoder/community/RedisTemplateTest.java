package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTemplateTest {
    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate template;

    @Test
    public void testStrings() {
        String redisKey = "test:count";
        template.opsForValue().set(redisKey, 1);
        System.out.println(template.opsForValue().get(redisKey));
        System.out.println(template.opsForValue().increment(redisKey));
        System.out.println(template.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHashes() {
        String redisKey = "test:user";
        template.opsForHash().put(redisKey, "id", "001");
        template.opsForHash().put(redisKey, "username", "nancy");
        System.out.println(template.opsForHash().get(redisKey, "id"));
        System.out.println(template.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testLists() {
        String redisKey = "test:teacher";
        template.opsForList().leftPush(redisKey, 101);
        template.opsForList().leftPush(redisKey, 102);
        template.opsForList().leftPush(redisKey, 103);
        System.out.println(template.opsForList().size(redisKey));
        System.out.println(template.opsForList().index(redisKey, 0));
        System.out.println(template.opsForList().range(redisKey, 0, 2));
        System.out.println(template.opsForList().leftPop(redisKey));
        System.out.println(template.opsForList().rightPop(redisKey));
        System.out.println(template.opsForList().rightPop(redisKey));
    }

    @Test
    public void testSets() {
        String redisKey = "test:students";
        template.opsForSet().add(redisKey, "tom", "nancy", "mike");
        System.out.println(template.opsForSet().size(redisKey));
        System.out.println(template.opsForSet().pop(redisKey));
        System.out.println(template.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSets() {
        String redisKey = "test:pets";
        template.opsForZSet().add(redisKey, "唐僧", 80);
        template.opsForZSet().add(redisKey, "悟空", 100);
        template.opsForZSet().add(redisKey, "八戒", 100);
        template.opsForZSet().add(redisKey, "莎莎", 60);
        System.out.println(template.opsForZSet().zCard(redisKey));
        System.out.println(template.opsForZSet().size(redisKey));
        System.out.println(template.opsForZSet().score(redisKey, "八戒"));
        System.out.println(template.opsForZSet().reverseRank(redisKey, "八戒"));
        System.out.println(template.opsForZSet().reverseRange(redisKey, 0, 2));
    }

    @Test
    public void testKeys() {
        template.delete("test:user");
        System.out.println(template.hasKey("test:user"));
        template.expire("test:students", 20, TimeUnit.SECONDS);
    }

    //多次访问同一个键
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        BoundValueOperations operations = template.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    //编程式事务
    @Test
    public void testTransactional() {
        Object obj = template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                redisOperations.multi();//开启事务

                redisOperations.opsForSet().add(redisKey, "mike");
                redisOperations.opsForSet().add(redisKey, "John");
                System.out.println(redisOperations.opsForSet().members(redisKey));//提交的时候才执行，所以这里返回空
                redisOperations.opsForSet().add(redisKey, "Julia");

                return redisOperations.exec();//提交事务，redis事务只有提交时才真正执行
            }
        });
        System.out.println(obj);
    }
}
