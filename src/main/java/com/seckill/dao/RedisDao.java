package com.seckill.dao;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by 史成成 on 2019/4/14.
 */
public class RedisDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    private RuntimeSchema<Seckill> runtimeSchema = RuntimeSchema.createFrom(Seckill.class);


    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    /**
     * 获得缓存
     * @param seckillId
     * @return
     */
    public Seckill getSeckill(long seckillId) {
        // return getSeckill(seckillId, null);
        try {
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:" + seckillId;
                byte[] bytes = jedis.get(key.getBytes());
                if (bytes != null) {
                    // 反序列化
                    // 1
                    Seckill  seckill = runtimeSchema.newMessage();
                    //
                    ProtobufIOUtil.mergeFrom(bytes, seckill, runtimeSchema);
                    return seckill;
                }
            }finally {
                jedis.close();
            }


        } catch (Exception e) {
            logger.error(e.getMessage());

        }

        return null;
    }

    /**
     * 设置redis缓存
     * @param seckill
     * @return
     */
    public String putSeckill(Seckill seckill) {
        try {
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:" + seckill.getSeckillId();
                byte[] bytes = ProtobufIOUtil.toByteArray(seckill, runtimeSchema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                int timeOut = 60 * 60;
                String result = jedis.setex(key.getBytes(), timeOut, bytes);
                return result;
            }finally {
                jedis.close();
            }


        } catch (Exception e) {
            logger.error(e.getMessage());

        }


        return null;
    }
}
