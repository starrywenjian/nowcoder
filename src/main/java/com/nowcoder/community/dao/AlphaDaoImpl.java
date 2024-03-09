package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository("HibernateBean")
public class AlphaDaoImpl implements AlphaDao{
    @Override
    public String select() {
        return "hello, nowcoder";
    }
}
