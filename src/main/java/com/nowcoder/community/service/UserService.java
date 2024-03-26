package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    //loginticket相关功能使用redis进行了代替
    @Autowired
    private RedisTemplate redisTemplate;

    //注入域名
    @Value("${community.path.domain}")
    private String domain;

    //注入项目路径
    @Value("${server.servlet.context-path}")
    private String contextPath;


    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            return initCache(id);
        }
        return user;
    }

    public User findUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //参数校验
        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            map.put("usernameMsg", "该用户名已存在");
            return map;
        }
        if (userMapper.selectByEmail(user.getEmail()) != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }
        //注册用户
        String salt = CommunityUtil.generateUUID().substring(0, 5);
        user.setSalt(salt);
        user.setPassword(CommunityUtil.md5(user.getPassword() + salt));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        //添加表数据
        userMapper.insert(user);
        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //url格式：http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        //入参校验
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            map.put("usernameMsg", "该帐户不存在");
            return map;
        }
        if (!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))) {
            map.put("passwordMsg", "密码错误");
            return map;
        }
        //验证激活状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该帐户尚未激活请先激活");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000L * expiredSeconds));
//        loginTicketMapper.insert(loginTicket);
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public LoginTicket getLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    public void updateHeader(int userId, String headerUrl) {
        User user = new User();
        user.setId(userId);
        user.setHeaderUrl(headerUrl);
        userMapper.updateByPrimaryKeySelective(user);
        cleanCache(userId);
    }

    public Map<String, Object> updatePassword(User user, String oldPassword, String newPassword, String confirmPassword) {
        Map<String, Object> map = new HashMap<>();
        // 校验密码
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(confirmPassword) || newPassword.equals(confirmPassword)) {
            map.put("confirmPasswordMsg", "两次密码输入不一致");
            return map;
        }
        // 假定登录状态已经被处理
        String salt = user.getSalt();
        newPassword = CommunityUtil.md5(newPassword + salt);
        //校验原密码
        if (!user.getPassword().equals(newPassword)) {
            map.put("oldPasswordMsg", "原密码输入错误");
            return map;
        }
        //校验通过，准备更新密码
        User newuser = new User();
        newuser.setId(user.getId());
        newuser.setPassword(newPassword);
        userMapper.updateByPrimaryKeySelective(newuser);
        cleanCache(user.getId());
        return map;
    }

    //优先从缓存中取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    //取不到时初始化缓存
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 1800, TimeUnit.SECONDS);
        return user;
    }

    //数据变更时删除缓存，没选择改缓存是考虑并发问题
    private void cleanCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

}
