package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.ElasticService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticService elasticService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    //search?keyword=xxx
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(String keyword, Model model, Page page) {
        org.springframework.data.domain.Page<DiscussPost> result = elasticService.search(keyword, page.getCurrent() - 1, page.getLimit());
        List<Map<String, Object>> discussPosts = null;
        if (result != null) {
            discussPosts = new ArrayList<>();
            for (DiscussPost discussPost : result) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);
        //处理分页信息,注意判空导致的处理
        page.setPath("/search?keyword=" + keyword);
        page.setRows(result == null ? 0 : (int) result.getTotalElements());
        return "/site/search";
    }
}
