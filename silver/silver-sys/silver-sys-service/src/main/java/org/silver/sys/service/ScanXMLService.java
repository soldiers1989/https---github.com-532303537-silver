package org.silver.sys.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class ScanXMLService {

	static String getEncoding() {
		return System.getProperty("file.encoding");
	}

	/**
	 * ftp登录 
	 * @param url 服务器
	 * @param port 端口号
	 * @param username 
	 * @param password
	 * @param encoding 连接操作采用字符
	 * @return
	 * @throws SocketException
	 * @throws IOException
	 */
	private static FTPClient FTPclientLogin(String url, int port, String username, String password, String encoding)
			throws SocketException, IOException {
		FTPClient ftpClient = new FTPClient();
		int reply;
		ftpClient.setControlEncoding(encoding);
		ftpClient.connect(url, port);
		ftpClient.login(username, password);
		// 设置文件传输类型为二进制
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		// 获取ftp登录应答代码
		reply = ftpClient.getReplyCode();
		// 设置访问被动模式
		ftpClient.setRemoteVerificationEnabled(false);
		ftpClient.enterLocalPassiveMode();
		// 验证是否登陆成功
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftpClient.disconnect();
			System.err.println("FTP server refused connection.");
		}
		return ftpClient;

	}

	public static boolean downFile(String url, int port, String username, String password, String remotePath,
			String localPath) {
		String encoding = getEncoding();
		FTPClient ftpClient = null;
		try {
			ftpClient = FTPclientLogin(url, port, username, password, getEncoding());
			// 转移到FTP服务器目录至指定的目录下
			ftpClient.changeWorkingDirectory(new String(remotePath.getBytes(encoding)));
			// 获取文件列表
			FTPFile[] fs = ftpClient.listFiles();
			OutputStream is = null;
			File localFile = null;
			for (FTPFile ff : fs) {
				if (!(ff.getName().equals("outime") || ff.getName().equals("tar"))) {
					localFile = new File(localPath + "/" + ff.getName());
					is = new FileOutputStream(localFile);
					ftpClient.retrieveFile(ff.getName(), is);
					try {
						DOMXMLService.getHeadBeanList(localFile); 
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
					is.close();
				}
			}
			for (FTPFile ff : fs) {
				ftpClient.deleteFile(ff.getName());// 扫描完毕删除文件
			}
			ftpClient.logout();
		} catch (SocketException e1) {
			e1.printStackTrace();
			return false;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		} finally {
			if (ftpClient != null && ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	
	
	public static void main(String[] args) {
	
		/*File f = new File("D://DOCREC_201707030956404400001_dataD20A51107A97113BEC246D55C5508468.xml");
		try {
			System.out.println(DOMXMLService.getHeadBeanList(f));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		Object a = 23;
	    int b =(int)a;
	    System.out.println(b);
	}
}
