package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post) {
        //参数校验
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        //转义字符处理
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //敏感词过滤
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        //插入
        return discussPostMapper.insert(post);
    }

    public DiscussPost discussPostDetail(int id) {
        return discussPostMapper.selectByPrimaryKey(id);
    }

    public void updateDisccusPostRows(int id, int counts) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(id);
        discussPost.setCommentCount(counts);
        discussPostMapper.updateByPrimaryKeySelective(discussPost);
    }

    public DiscussPost findDiscussPostById(int postId){
        return discussPostMapper.selectByIdDiscussPost(postId);
    }

}
