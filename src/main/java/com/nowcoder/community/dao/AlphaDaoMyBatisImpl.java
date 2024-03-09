package com.nowcoder.community.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("AlphaDaoMyBatis")
@Primary
public class AlphaDaoMyBatisImpl implements AlphaDao {
    @Override
    public String select() {
        return "hello, mybatis bean";
    }
}
