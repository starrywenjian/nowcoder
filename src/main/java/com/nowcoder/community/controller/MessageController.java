package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getMessage(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setPath("/letter/list");
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));
        List<Message> conversationList = messageService.findConversation(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversationVoList = new ArrayList<>();
        if (conversationList != null) {
            for (Message conversation : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", conversation);
                int target = conversation.getFromId() == user.getId() ? conversation.getToId() : conversation.getFromId();
                map.put("target", userService.findUserById(target));
                map.put("letterCount", messageService.findLetterCount(conversation.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), conversation.getConversationId()));
                conversationVoList.add(map);
            }
        }
        //查询未读消息数量
        model.addAttribute("conversations", conversationVoList);
        model.addAttribute("letterUnreadCount", messageService.findLetterUnreadCount(user.getId(), null));
        model.addAttribute("noticeUnreadCount", messageService.findUnreadNoticeCount(user.getId(), null));

        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(Model model, Page page, @PathVariable("conversationId") String conversationId) {
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        //查信息
        List<Message> letters = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letterVoList = new ArrayList<>();
        if (letters != null) {
            for (Message letter : letters) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("fromUser", userService.findUserById(letter.getFromId()));
                letterVoList.add(map);
            }
        }
        model.addAttribute("letters", letterVoList);
        //私信目标
        model.addAttribute("target", getLetterTarget(conversationId));
        //更新未读消息状态
        List<Integer> ids = this.getUnreadLetterIds(letters);
        if (!ids.isEmpty()) {
            messageService.readLetter(ids, 1);
        }

        return "/site/letter-detail";
    }

    /**
     * 在 Spring 中，@ResponseBody 注解通常用于指示一个方法返回的结果直接作为 HTTP 响应的主体内容，而不是视图名称。
     * 这在处理异步请求时是很常见的，因为异步请求通常期望返回的是数据而不是 HTML 页面。
     */

    @RequestMapping(value = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String addLetter(String toName, String content) {
        User target = userService.findUserByUsername(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "系统中不存在此用户");
        }
        Message letter = new Message();
        letter.setContent(HtmlUtils.htmlEscape(content));
        letter.setContent(sensitiveFilter.filter(letter.getContent()));
        letter.setStatus(0);
        letter.setFromId(hostHolder.getUser().getId());
        letter.setToId(target.getId());
        letter.setCreateTime(new Date());
        String conversationId;
        if (target.getId() < hostHolder.getUser().getId()) {
            conversationId = target.getId() + "_" + hostHolder.getUser().getId();
        } else {
            conversationId = hostHolder.getUser().getId() + "_" + target.getId();
        }
        letter.setConversationId(conversationId);
        messageService.addLetter(letter);

        return CommunityUtil.getJSONString(0);
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.valueOf(ids[0]);
        int id1 = Integer.valueOf(ids[1]);
        int target = id0 == hostHolder.getUser().getId() ? id1 : id0;
        return userService.findUserById(target);
    }

    private List<Integer> getUnreadLetterIds(List<Message> letters) {
        List<Integer> ids = new ArrayList<>();
        if (letters != null) {
            for (Message letter : letters) {
                // 如果直接使用==号，超出128的id会判false，这是个坑。。。
                if (letter.getToId().equals(hostHolder.getUser().getId()) && letter.getStatus() == 0) {
                    ids.add(letter.getId());
                }
            }
        }

        return ids;
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String noticeList(Model model) {
        //评论类通知
        Message notice = messageService.findLatestNotice(hostHolder.getUser().getId(), TOPIC_COMMENT);
        Map<String, Object> messageVo = null;
        if (notice != null) {
            messageVo = new HashMap<>();
            messageVo.put("message", notice);
            Map<String, Object> data = JSONObject.parseObject(notice.getContent(), HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            messageVo.put("unread", messageService.findLetterUnreadCount(hostHolder.getUser().getId(), TOPIC_COMMENT));
            messageVo.put("count", messageService.findNoticeCount(hostHolder.getUser().getId(), TOPIC_COMMENT));
        }
        model.addAttribute("commentNotice", messageVo);
        //点赞类通知
        notice = messageService.findLatestNotice(hostHolder.getUser().getId(), TOPIC_LIKE);
        messageVo = null;
        if (notice != null) {
            messageVo = new HashMap<>();
            messageVo.put("message", notice);
            Map<String, Object> data = JSONObject.parseObject(notice.getContent(), HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));
            messageVo.put("unread", messageService.findLetterUnreadCount(hostHolder.getUser().getId(), TOPIC_LIKE));
            messageVo.put("count", messageService.findNoticeCount(hostHolder.getUser().getId(), TOPIC_LIKE));
        }
        model.addAttribute("likeNotice", messageVo);
        //关注类通知
        notice = messageService.findLatestNotice(hostHolder.getUser().getId(), TOPIC_FOLLOW);
        messageVo = null;
        if (notice != null) {
            messageVo = new HashMap<>();
            messageVo.put("message", notice);
            Map<String, Object> data = JSONObject.parseObject(notice.getContent(), HashMap.class);
            messageVo.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("unread", messageService.findLetterUnreadCount(hostHolder.getUser().getId(), TOPIC_FOLLOW));
            messageVo.put("count", messageService.findNoticeCount(hostHolder.getUser().getId(), TOPIC_FOLLOW));
        }
        model.addAttribute("followNotice", messageVo);
        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(hostHolder.getUser().getId(), null);
        int noticeUnreadCount = messageService.findUnreadNoticeCount(hostHolder.getUser().getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String noticeDetail(@PathVariable("topic") String topic, Model model, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticesVoList = null;
        if (notices != null) {
            noticesVoList = new ArrayList<>();
            for (Message notice : notices) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                Map<String, Object> data = JSONObject.parseObject(notice.getContent(), HashMap.class);
                //内容
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));// 这里不用担心关注事件无post，反正放进去一个空的也没关系
                //通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticesVoList.add(map);
            }
        }
        model.addAttribute("notices", noticesVoList);

        //设置已读
        messageService.readNotice(user.getId(), topic);

        return "site/notice-detail";
    }
}












