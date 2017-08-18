package org.silver.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.piccolo.xml.XMLInputReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.silver.config.ConfigManager;

/**
 * FTP 相关操作
 * 
 * @author jinlei
 */
public class FtpUtils {
	private static final Logger LOGGER = Logger.getLogger(FtpUtils.class);
	// 连接FTP服务器超时时
	private static final int CONNECTION_TIME_OUT = 60 * 1000;
	// 获取数据超时时间
	private static final int READ_DATA_TIME_OUT = 30 * 60 * 1000;

	private static FTPClient instance = null;

	private FtpUtils() {

	}

	public static FTPClient getConnection() {
		if (null == instance) {
			instance = new FTPClient();
			try {
				instance.setConnectTimeout(CONNECTION_TIME_OUT);
				instance.connect(ConfigManager.getInstance().getFtpIP(), ConfigManager.getInstance().getFtpPort());
				instance.login(ConfigManager.getInstance().getFtpUsername(),
						ConfigManager.getInstance().getFtpPassword());
				instance.setControlEncoding("UTF-8");
				instance.enterLocalPassiveMode();
				instance.setDataTimeout(READ_DATA_TIME_OUT);
				instance.setFileType(FTPClient.BINARY_FILE_TYPE);
				instance.changeWorkingDirectory("/");
				LOGGER.info("|--------------- 连接海关 FTP文件服务器成功");
			} catch (Exception e) {
				LOGGER.error("|--------------- 连接海关 FTP文件服务器失败");
				e.printStackTrace();
			}
			return instance;
		}
		return instance;
	}

	/***
	 * 获取FTP服务器目录列
	 * 
	 * @param client
	 * @param path
	 * @return
	 */
	public static FTPFile[] getFileNames(final String path) {
		FTPFile[] ftpFileNames = null;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		FutureTask<FTPFile[]> futureTask = new FutureTask<FTPFile[]>(new Callable<FTPFile[]>() {
			public FTPFile[] call() {
				// 真正的任务，这里的返回类型为String，可以为任意类型
				return getFTPFiles(path);
			}
		});
		executor.execute(futureTask);
		try {
			// 取得结果，同时设置超时执行时间为5秒同样可以用future.get()，不设置执行超时时间取得结果
			ftpFileNames = futureTask.get(5000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			futureTask.cancel(true);
			// 超时后，进行相应处理
		} finally {
			executor.shutdown();
		}
		return ftpFileNames;
	}

	public static FTPFile[] getFTPFiles(String path) {
		FTPClient client = getConnection();
		try {
			client.changeWorkingDirectory(path);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		FTPFile[] files = null;
		try {
			files = client.listFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return files;
	}

	/***
	 * 关闭FTP连接
	 * 
	 * @param client
	 */
	public static void closeFtpConnection(FTPClient client) {
		try {
			if (client != null) {
				client.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("|--------------- 关闭FTP服务器失败");
		}
	}

	/**
	 * @param file
	 *            上传的文件或文件夹
	 * @param workPath
	 *            工作目录
	 * @throws Exception
	 */
	public static String upload(File file, String workPath) throws Exception {
		FTPClient ftp = getConnection();
		ftp.changeWorkingDirectory(workPath);
		if (file.isDirectory()) {
			ftp.makeDirectory(file.getName());
			ftp.changeWorkingDirectory(file.getName());
			String[] files = file.list();
			for (int i = 0; i < files.length; i++) {
				File file1 = new File(file.getPath() + "\\" + files[i]);
				if (file1.isDirectory()) {
					upload(file1, workPath);
					ftp.changeToParentDirectory();
				} else {
					File file2 = new File(file.getPath() + "\\" + files[i]);
					FileInputStream input = new FileInputStream(file2);
					ftp.storeFile(file2.getName(), input);
					input.close();
				}
			}
		} else {
			File file2 = new File(file.getPath());
			FileInputStream input = new FileInputStream(file2);
			ftp.storeFile(file2.getName(), input);
			input.close();
		}
		return "";
	}

	/**
	 * @param file
	 *            南沙下载文件或文件夹
	 * @param serverWorkPath
	 *            工作目录
	 * @throws Exception
	 * @return
	 * @throws Exception
	 */
	public static String downLoad(String serverWorkPath) throws Exception {
		FTPClient ftp = getConnection();
		ftp.changeWorkingDirectory(serverWorkPath);
		OutputStream is = null;
		try {
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				File localFile = new File("D://work" + "/" + ff.getName());
				is = new FileOutputStream(localFile);
				ftp.retrieveFile(ff.getName(), is);
				System.out.println(ff.getName());
				SAXBuilder builder=new SAXBuilder(false); 
			
				Document doc=builder.build("D://work" + "/" + ff.getName());
				
				
				
				
				is.close();
			}
			ftp.logout();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	
	
	
	public static void main(String[] args) {
		FtpUtils ftpClient = new FtpUtils();
		List<String> list = new ArrayList<>();
		list.add("DOCREC_201706281109199990001_data713B7493EB964EB0F21C7DF1338D3DBA.xml");
		try {
			ftpClient.downLoad("/4200.IMPBA.SWBCARGOBACK.REPORT/out");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
