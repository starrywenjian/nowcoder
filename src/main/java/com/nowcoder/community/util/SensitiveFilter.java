package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    //根节点
    private TrieNode root = new TrieNode();

    // 替换符
    private static final String REPLACEMENT = "***";

    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        TrieNode temp = root;
        StringBuilder sb = new StringBuilder();
        int begin = 0, position = 0;
        while (position < text.length()) {
            char c = text.charAt(position);
            //判断特殊符号
            if (isSymbolic(c)) {
                if (temp == root) {
                    //指针1处于根节点，将此符合计入结果，让指针2向下走一步
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //检查下级节点
            TrieNode subNode = temp.getSubNode(c);
            if (subNode == null) {
                //以Begin开头的字符串不是敏感词
                sb.append(text.substring(begin,position + 1));
                //进入下一个位置
                begin = ++position;
                temp = root;
            } else if (subNode.isKeyWordEnd()) {
                //发现敏感词进行替换
                sb.append(REPLACEMENT);
                begin = ++position;
                temp=root;
            } else {
                //检查下一个字符
                temp = subNode;
                position++;
            }
        }

        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbolic(char c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitiveword.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyWord;
            while ((keyWord = reader.readLine()) != null) {
                this.addKeyWord(keyWord);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    private void addKeyWord(String keyWord) {
        TrieNode tempNode = root;
        for (int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addNode(c, subNode);
            }
            tempNode = subNode;
            if (i == keyWord.length() - 1) {
                //到达结尾添加敏感词标记
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    private class TrieNode {
        private boolean isKeyWordEnd;// 敏感词结尾标记
        //使用map封装子节点(key是下级字符，value是下级节点)
        private Map<Character, TrieNode> map = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        public void addNode(Character c, TrieNode node) {
            map.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return map.get(c);
        }
    }
}
