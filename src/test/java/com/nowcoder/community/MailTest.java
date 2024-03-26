package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.Context;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSendMail() {
        mailClient.sendMail("1195586807@qq.com", "test send mail success", "hello, myself!");
    }

    @Test
    public void testSendHtmlMail() {
        Context context = new Context(); // 使用模板因其进行渲染
        context.setVariable("username", "Saturday");
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("1195586807@qq.com", "test send html mail", content);
    }

    @Test
    public void testSubString() {
        String text = "anbc";
        String temp =text.substring(4);
    }
}
