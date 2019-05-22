package com.seckill.service.impl;

import com.seckill.dao.SeckillDao;
import com.seckill.dao.SuccessKilledDao;
import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.entity.Seckill;
import com.seckill.entity.SuccessKilled;
import com.seckill.enums.SeckillStatEnum;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import com.seckill.exception.SeckillException;
import com.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 史成成 on 2019/4/10.
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    //md5盐值
    private final String slat="swtr##$#$fxbvj*&%$uy(())kiumhcm";

    public static void main(String[] args) {
        //ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:spring/spring-service.xml");

        ApplicationContext ac1 = new ClassPathXmlApplicationContext("classpath:spring/spring-dao.xml");
        SeckillServiceImpl seckillService = ac1.getBean(SeckillServiceImpl.class);
        System.out.println(seckillService.getById(1000));
    }


    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = seckillDao.queryById(seckillId);
        if (seckill == null) {
            return new Exposer(false, seckillId);
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        String md5 = getMd5(seckillId); // TODO: 2019/4/10
        return new Exposer(true, md5, seckillId);
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        try {
            if (md5 == null || !md5.equals(getMd5(seckillId))) {

                return new SeckillExecution(seckillId, SeckillStatEnum.DATE_REWRITE);
            }
            // 业务：减库存+加记录
            // 先插入记录，后更新记录可以降低重复秒杀的时间
            //插入秒杀记录
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0) {
                throw new RepeatKillException("seckill repeat");
            }
            // 更新库存
            Date nowTime = new Date();
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if (updateCount <= 0) {
                // rollback
                throw new SeckillCloseException("seckill closed");
            }
            SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
            //commit
            return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);
        }catch (SeckillCloseException e){
            throw e;

        }catch (SeckillException e){
            throw e;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // 将编译期异常转化为运行期异常
            throw new SeckillException("error" + e.getMessage());
        }

    }

    @Override
    public SeckillExecution executeSeckillByProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMd5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatEnum.DATE_REWRITE);
        }

        Date killTime = new Date();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        try {
            seckillDao.killByProcedure(map);
            int result = Integer.parseInt(map.get("result").toString());
            //秒杀成功
            if (result == 1) {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
            } else {
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
        }

    }

    /**
     * 私有函数用于md5加密
     * @param seckillId
     * @return
     */
    private String getMd5(long seckillId) {
        String base = seckillId + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    public SeckillDao getSeckillDao() {
        return seckillDao;
    }
}
