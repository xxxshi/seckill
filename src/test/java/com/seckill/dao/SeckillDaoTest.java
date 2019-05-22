package com.seckill.dao;

import com.seckill.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by 史成成 on 2019/4/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-dao.xml")
public class SeckillDaoTest {
    @Resource
    private SeckillDao seckillDao;



    @Test
    public void testQueryById() throws Exception {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill);
    }

    @Test
    public void testQueryAll() throws Exception {
        int offset = 0;
        int limit = 10;
        List<Seckill> list = seckillDao.queryAll(offset, limit);
        for (Seckill seckill : list) {
            System.out.println(seckill);
        }
    }

    @Test
    public void testReduceNumber() throws Exception {
        long id = 1000;
        int result = seckillDao.reduceNumber(id, new Date());
        System.out.println("result:"+result);

    }
}