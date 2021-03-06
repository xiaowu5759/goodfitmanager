package com.simba.goodfitmanager.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * 对RedisTemplate 再一次的封装
 * 对于数据库的增删改查
 * 加上内存的过期时间
 */
@Component
public class RedisUtil {
    @Value("${token.expirationSeconds}")
    private int expirationSeconds;

    // 常量，各种实现方式，这里读取application.yml
    @Value("${token.validTime}")
    private int validTime;

    @Autowired
    private StringRedisTemplate redisTemplate;
//    private RedisTemplate<String,Object> redisTemplate = new  RedisTemplate<>();

//    @Autowired
//    public RedisUtil(StringRedisTemplate redisTemplate){
//        this.redisTemplate = redisTemplate;
//    }

    /**
     * 查询key,支持模糊查询
     * 传过来的key的前后端已经加入了*
     * @param key
     * @return
     */
    public Set<String> keys(String key){
        return redisTemplate.keys(key);
    }

    /**
     * 根据键值获取值
     * @param key
     * @return
     */
    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 字符串存入值
     * 默认过期时间是2小时
     * @param key
     * @param value
     */
    public void set(String key,String value){
        redisTemplate.opsForValue().set(key, value,7200,TimeUnit.SECONDS);
    }

    /**
     * 存入字符串
     * 自定义失效时间
     * @param key
     * @param value
     * @param expire
     */
    public void set(String key,String value,Integer expire){
        redisTemplate.opsForValue().set(key,value,expire);
    }

    /**
     * 删除key
     * @param key
     */
    public void delete(String key){
        redisTemplate.opsForValue().getOperations().delete(key);
    }

    /**
     * 添加单个
     * 默认过期时间为两小时
     * @param key    key
     * @param filed  filed
     * @param domain 对象
     */
    public void hset(String key,String filed,Object domain){
        redisTemplate.opsForHash().put(key, filed, domain);
    }

    /**
     * 添加单个
     * @param key    key
     * @param filed  filed
     * @param domain 对象
     * @param expire 过期时间（毫秒计）
     */
    public void hset(String key,String filed,Object domain,Integer expire){
//        if (redisTemplate != null) {
//
//            redisTemplate.opsForHash().put(key, filed, domain);
//            redisTemplate.expire(key, expire,TimeUnit.SECONDS);
//        }
//        System.out.println("redisTemplate为空");
//        return ;
        redisTemplate.opsForHash().put(key, filed, domain);
        redisTemplate.expire(key, expire,TimeUnit.SECONDS);

    }

    /**
     * 添加HashMap
     *
     * @param key    key
     * @param hm    要存入的hash表
     */
    public void hset(String key, HashMap<String,Object> hm){
        redisTemplate.opsForHash().putAll(key,hm);
    }

    /**
     * 如果key存在就不覆盖
     * @param key
     * @param filed
     * @param domain
     */
    public void hsetAbsent(String key,String filed,Object domain){
        redisTemplate.opsForHash().putIfAbsent(key, filed, domain);
    }

    /**
     * 查询key和field所确定的值
     *
     * @param key 查询的key
     * @param field 查询的field
     * @return HV
     */
    public Object hget(String key,String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 查询该key下所有值
     *
     * @param key 查询的key
     * @return Map<HK, HV>
     */
    public Object hget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除key下所有值
     *
     * @param key 查询的key
     */
    public void deleteKey(String key) {
        redisTemplate.opsForHash().getOperations().delete(key);
    }

    /**
     * 判断key和field下是否有值
     *
     * @param key 判断的key
     * @param field 判断的field
     */
    public Boolean hasKey(String key,String field) {
        return redisTemplate.opsForHash().hasKey(key,field);
    }

    /**
     * 判断key下是否有值
     *
     * @param key 判断的key
     */
    public Boolean hasKey(String key) {
        return redisTemplate.opsForHash().getOperations().hasKey(key);
    }

    /**
     * 判断此token是否在黑名单中
     * @param token
     * @return
     */
    public Boolean isBlackList(String token){
        return hasKey("blacklist",token);
    }

    /**
     * 将token加入到redis黑名单中
     * @param token
     */
    public void addBlackList(String token){
        hset("blacklist", token,DateUtil.getTime());
    }


    /**
     * 查询token下的刷新时间
     *
     * @param token 查询的key
     * @return HV
     */
    public Object getTokenValidTimeByToken(String token) {
        return redisTemplate.opsForHash().get(token, "tokenValidTime");
    }

    /**
     * 查询token下的 用户名
     *
     * @param token 查询的key
     * @return HV
     */
    public Object getUsernameByToken(String token) {
        return redisTemplate.opsForHash().get(token, "username");
    }

    /**
     * 查询token下的 ip地址
     *
     * @param token 查询的key
     * @return HV
     */
    public Object getIPByToken(String token) {
        return redisTemplate.opsForHash().get(token, "ip");
    }

    /**
     * 查询token下的过期时间
     *
     * @param token 查询的key
     * @return HV
     */
    public Object getExpirationTimeByToken(String token) {
        return redisTemplate.opsForHash().get(token, "expirationTime");
    }

    public void setToken(String token,String username,String ip){
//        设置在redis的有效时间
        Integer expire = validTime*24*60*60;
//        Integer expire = 60;

        System.out.println(validTime+expirationSeconds+expire+username+ip);
        hset(token, "tokenValidTime",DateUtil.getAddDayTime(validTime),expire);
        hset(token, "expirationTime",DateUtil.getAddDaySecond(expirationSeconds),expire);
        hset(token, "username",username,expire);
        hset(token, "ip",ip,expire);
//        hset("blacklist", token,DateUtil.getTime());

    }

    public void setTokenRefresh(String token,String username,String ip,String refreshValidTime ){
        //设置在redis的有效时间
        Integer expire = Integer.valueOf(refreshValidTime)*24*60*60;

        hset(token, "tokenValidTime",refreshValidTime,expire);
        hset(token, "expirationTime",DateUtil.getAddDaySecond(expirationSeconds),expire);
        hset(token, "username",username,expire);
        hset(token, "ip",ip,expire);
    }

}
