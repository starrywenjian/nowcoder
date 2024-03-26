package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectDiscussPosts() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testSelectUserById() {
        User user = userMapper.selectById(149);
        System.out.println(user);
    }

    @Test
    public void testInsertTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket("abc");
        loginTicket.setUserId(101);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 300 * 1000));
        loginTicket.setStatus(0);
        loginTicketMapper.insert(loginTicket);
    }

    @Test
    public void testSelectTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testUpdateStatus() {
        loginTicketMapper.updateStatus("abc", 1);
    }

    @Test
    public void testSelectConversation() {
        int i = messageMapper.selectConvesrsationCount(111);
        System.out.println(i);
        List<Message> messages = messageMapper.selectConversations(111, 0, 5);
        for (Message message : messages) {
            System.out.println(message);
        }
    }

    @Test
    public void testSelectLetters() {
        List<Message> messages = messageMapper.selectLetters("111_112", 0, 5);
        for (Message message : messages) {
            System.out.println(message);
        }
        int count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

    }

    @Test
    public void testLetterUnreadCount() {
        int totalcount = messageMapper.selectLetterUnreadCount(111, null);
        System.out.println("total: " + totalcount);
        int co = messageMapper.selectLetterUnreadCount(111, "111_112");
        System.out.println("count: " + co);

    }

    @Test
    public void testNotice() {
        Message message = messageMapper.selectLatestNotice(111, "comment");
        System.out.println(message);
        int totalUnread = messageMapper.selectUnreadNoticeCount(111, null);
        int followUnread = messageMapper.selectUnreadNoticeCount(111, "follow");
        int likeUnread = messageMapper.selectUnreadNoticeCount(111, "like");
        int commentUnread = messageMapper.selectUnreadNoticeCount(111, "comment");
        int totalLikeCount = messageMapper.selectNoticeCount(111, "like");
        System.out.println("totalunread: " + totalUnread);
        System.out.println("followunread:" + followUnread);
        System.out.println("likeUnread: " + likeUnread);
        System.out.println("commentUnread: " + commentUnread);
        System.out.println("total: " + totalLikeCount);

    }

}
