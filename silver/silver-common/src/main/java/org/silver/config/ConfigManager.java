package org.silver.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * 读取配置文件
 * @author jinlei
 */
public class ConfigManager {
	/***
	 * 日志管理
	 */
	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class);

	private static final String DEFAULT_FILE_NAME = "config/systemConfig.properties";

	private static ConfigManager configManager = null;

	private Map<String, String> initParam = new HashMap<String, String>();

	private static final String FTP_IP = "ftp.ip";
	private static final String FTP_USERNAME = "ftp.username";
	private static final String FTP_PASSWORD = "ftp.password";
	private static final String FTP_PORT = "ftp.port";

	private ConfigManager() {
	}

	public static synchronized ConfigManager getInstance() {
		if (null == configManager) {
			configManager = new ConfigManager();
			configManager.init();
		}
		return configManager;
	}

	/***
	 * 初始化方
	 */
	public void init() {
		// 加载配置文件
		onload();
	}

	/***
	 * 加载配置文件信息
	 */
	private void onload() {
		Properties properties = new Properties();
		InputStream in = null;
		try {
			in = getClass().getClassLoader().getResourceAsStream(DEFAULT_FILE_NAME);
			properties.load(in);
			Set<Entry<Object, Object>> entrySet = properties.entrySet();
			for (Entry<Object, Object> entry : entrySet) {
				initParam.put(String.valueOf(entry.getKey()),
						String.valueOf(entry.getValue()));
			}
		} catch (IOException e) {
			LOGGER.error(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	/***
	 * @param key
	 * @return
	 */
	public String getConfig(String key) {
		return initParam.get(key);
	}
 

	/***
	 * 获取ftpIp
	 * 
	 * @return
	 */
	public String getFtpIP() {
		return getConfig(FTP_IP);
	}

	/***
	 * 获取Ftp username
	 * 
	 * @return
	 */
	public String getFtpUsername() {
		return getConfig(FTP_USERNAME);
	}

	/***
	 * 获取ftp password
	 * 
	 * @return
	 */
	public String getFtpPassword() {
		return getConfig(FTP_PASSWORD);
	}
	
	/***
	 * 获取ftp port
	 * 
	 * @return
	 */
	public Integer getFtpPort() {
		String port = getConfig(FTP_PORT);
		try{
			Integer.valueOf(port);
		}catch(Exception e){
			//如果端口没有配置或者配置的不是数字，采用默认端口
			LOGGER.info("没有配置ftp 端口或者配置的不是数字，采用默认端口");
			return 21;
		}
		return Integer.valueOf(port);
	}
}
