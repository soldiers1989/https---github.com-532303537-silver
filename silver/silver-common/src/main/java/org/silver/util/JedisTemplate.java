package org.silver.util;

import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * @author Alvis
 * @version 2016年5月20日 下午3:19:19
 */
@PropertySource("classpath:config/redis.properties")
public class JedisTemplate {
	private static final Logger logger = LogManager.getLogger();

	private static ShardedJedisPool shardedJedisPool = null;

	private static Integer EXPIRE = Integer
			.valueOf(ResourceBundle.getBundle("config/redis").getString("redis.expiration"));

	// 获取线程
	private static ShardedJedis getJedis() {
		if (shardedJedisPool == null) {
			synchronized (EXPIRE) {
				if (shardedJedisPool == null) {
					WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
					shardedJedisPool = wac.getBean(ShardedJedisPool.class);
				}
			}
		}
		return shardedJedisPool.getResource();
	}

	public static <K> K run(String key, Executor<K> executor, Integer... expire) {
		ShardedJedis jedis = getJedis();
		if (jedis == null) {
			return null;
		}
		try {
			K result = executor.execute(jedis);
			if (jedis.exists(key)) {
				if (expire == null || expire.length == 0) {
					// 获取剩余有效期
					int ttl = jedis.ttl(key).intValue();
					jedis.expire(key, ttl);
				} else if (expire.length == 1) {
					jedis.expire(key, expire[0]);
				}
			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return null;
	}

	public static <K> K run(byte[] key, Executor<K> executor, Integer... expire) {
		ShardedJedis jedis = getJedis();
		if (jedis == null) {
			return null;
		}
		try {
			K result = executor.execute(jedis);
			if (jedis.exists(key)) {
				if (expire == null || expire.length == 0) {
					// 获取剩余有效期
					int ttl = jedis.ttl(key).intValue();
					jedis.expire(key, ttl);
				} else if (expire.length == 1) {
					jedis.expire(key, expire[0]);
				}
			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return null;
	}

	public static void main(String[] args) {

		// Jedis jedis = new Jedis("150.242.58.22", 6380);
		// jedis.auth("jugg");
		// jedis.set("name", "678");
		// System.out.println(jedis.get("name"));
	}
}
