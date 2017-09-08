package org.silver.sys.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Date;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.silver.common.NSFtpConfig;



public class FtpUtil {
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
		System.out.println("|--------------- 连接 FTP文件服务器成功");
		return ftpClient;

	}
	
	/**
	 * 上传文件   
	 * @param url  FTP服务器hostname
	 * @param port FTP服务器端口
	 * @param username FTP登录账号
	 * @param password FTP登录密码
	 * @param remotePath FTP服务器上的相对路径
	 * @param file 要上传的文件名
	 * @return
	 * @throws Exception
	 */
    public static String upload(String url,int port,String username,String password,String remotePath,File file) throws Exception{     
    	FTPClient ftp = FTPclientLogin(url, port, username, password, "utf-8");
    	// 转移到FTP服务器目录至指定的目录下
    	ftp.changeWorkingDirectory(remotePath);
        if(file.isDirectory()){           
            ftp.makeDirectory(file.getName());                
            ftp.changeWorkingDirectory(file.getName());      
            String[] files = file.list();             
            for (int i = 0; i < files.length; i++) {      
                File file1 = new File(file.getPath()+"\\"+files[i] );      
                if(file1.isDirectory()){//检查一个对象是否是文件夹      
                    upload(url,port,username,password,remotePath,file1);      
                    ftp.changeToParentDirectory();
                }else{                    
                    File file2 = new File(file.getPath()+"\\"+files[i]);      
                    FileInputStream input = new FileInputStream(file2);      
                    ftp.storeFile(file2.getName(), input);      
                    input.close();                            
                }                 
            }  
        }else{      
            File file2 = new File(file.getPath());      
            FileInputStream input = new FileInputStream(file2);      
            ftp.storeFile(file2.getName(), input);      
            input.close();        
        }     
        return "";
    } 
    /**
     * 
     * @param url        FTP服务器hostname
     * @param port       FTP服务器端口
     * @param username   FTP登录账号
     * @param password   FTP登录密码
     * @param remotePath 指定FTP路径
     * @param fileName   指定文件名
     * @param localPath  下载路径
     * @return
     * @throws SocketException
     * @throws IOException
     */
    public static boolean downFile(String url, int port, String username, String password, String remotePath,
			String fileName, String localPath) throws SocketException, IOException {
		boolean result = false;
		FTPClient ftpClient = FTPclientLogin(url, port, username, password, "utf-8");
		String encoding = System.getProperty("file.encoding");
		try {
			int reply;
			// 转移到FTP服务器目录至指定的目录下
			ftpClient.changeWorkingDirectory(new String(remotePath.getBytes(encoding)));
			// 获取文件列表
			FTPFile[] fs = ftpClient.listFiles();
			OutputStream is = null;
			File localFile = null;
//			if (fs != null && fs.length > 2) {
				for (FTPFile ff : fs) {
					// if (ff.getName().equals(fileName)) {
					if (!(ff.getName().equals("outime") || ff.getName().equals("tar"))) {
						//文件生成所在目录
						String path = localPath+"/"+DateUtil.formatDate(new Date(), "yyyyMMdd");
						File uploadFile = new File(path); // 
						if (!uploadFile.exists() || uploadFile == null) { // 
							uploadFile.mkdirs();
						}
						path = uploadFile.getPath() + "\\" + ff.getName();
						localFile = new File(path);
						is = new FileOutputStream(localFile);
						ftpClient.retrieveFile(ff.getName(), is);
						try {
							DOMXMLService.getHeadBeanList(localFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
						is.close();
					}

					// }
				}
//				for (FTPFile ff : fs) {
//					System.out.println(ftpClient.deleteFile(ff.getName()));
//				}
//			}

			System.out.println("delete");
			ftpClient.logout();
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException ioe) {
				}
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
//		try {
			File file = new File("C:/work/order/66110120170802102831000.xml");
			try {
				FtpUtil.upload(NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT,
						NSFtpConfig.FTP_USER_NAME_YM,
						NSFtpConfig.FTP_PASS_WORD_YM, 
						"/4200.IMPBA.SWBEBTRADE.REPORT/in/",
						file);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			Boolean fTPClient =FtpUtil.downFile(NSFtpConfig.FTP_ID, 
//					NSFtpConfig.FTP_PORT, 
//					NSFtpConfig.FTP_USER_NAME_YM, 
//					NSFtpConfig.FTP_PASS_WORD_YM, "/4200.IMPBA.SWBEBTRADE.REPORT/out/", "", "f:/tools/download");
//			fTPClient.disconnect();
//			System.out.println(fTPClient.isConnected());
			
//		} catch (SocketException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
		
}
