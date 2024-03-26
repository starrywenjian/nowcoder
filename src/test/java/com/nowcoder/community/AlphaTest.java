package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class AlphaTest {
    @Autowired
    private AlphaService alphaService;

    @Test
    public void testFilter() {
        Object o = alphaService.save1();
        System.out.println(o);
    }

    @Test
    public void testSave2() {
        Object o = alphaService.save2();
        System.out.println(o);
    }

    @Test
    public void testHashMap() {
        Map<String, Object> m = new HashMap<>(); // undefined
        System.out.println(m.get("message"));
    }
}
