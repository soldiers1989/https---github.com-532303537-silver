package org.silver.util;

import redis.clients.jedis.ShardedJedis;

/**
 * @author Alvis
 * @version 2016年5月20日 下午3:19:19
 */
public interface Executor<K> {
	public K execute(ShardedJedis jedis);
}
