package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UnreadInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;


    //在通过controller，渲染模板之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (hostHolder.getUser() != null && modelAndView != null) {
            int unreadNoticeCount = messageService.findUnreadNoticeCount(hostHolder.getUser().getId(), null);
            int letterUnreadCount = messageService.findLetterUnreadCount(hostHolder.getUser().getId(), null);
            modelAndView.addObject("allUnreadCount", unreadNoticeCount + letterUnreadCount);
        }
    }
}
