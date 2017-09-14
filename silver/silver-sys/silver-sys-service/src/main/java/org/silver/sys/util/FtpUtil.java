package org.silver.sys.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.silver.common.GZFtpConfig;
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
    public static Map<String, Object> downFile(String url, int port, String username, String password, String remotePath,
			String fileName, String localPath) throws SocketException, IOException {
    	Map<String, Object> statusMap = new HashMap<String, Object>();
    	List pathList =  new ArrayList<>();
		FTPClient ftpClient = FTPclientLogin(url, port, username, password, "utf-8");
		String encoding = System.getProperty("file.encoding");
		try {
			// 转移到FTP服务器目录至指定的目录下
			ftpClient.changeWorkingDirectory(new String(remotePath.getBytes(encoding)));
			// 获取文件列表
			FTPFile[] fs = ftpClient.listFiles();
			OutputStream is = null;
			File localFile = null;
				for (FTPFile ff : fs) {
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
						is.close();
						pathList.add(path);
					}
				}
				statusMap.put("status", 1);
				statusMap.put("msg", "报文下载成功 ");
				statusMap.put("path", pathList);
//				for (FTPFile ff : fs) {
//					System.out.println(ftpClient.deleteFile(ff.getName()));
//			    }
			System.out.println("---FTP服务器回执报文清除完毕！---");
			ftpClient.logout();
		} catch (IOException e) {
			e.printStackTrace();
			statusMap.put("status", 1);
			statusMap.put("msg", "报文下载失败 ");
			return statusMap;
		} finally {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException ioe) {
				}
			}
		}
		return statusMap;
	}
	
	public static void main(String[] args) {
		//上传文件
//		File file = new File("F:/tools/download/20170911/DOCREC_201708031044225020001_dataA1FE78E12B8E867EA46AB6D2A61F1A87.xml");
//		try {
//			FtpUtil.upload(NSFtpConfig.FTP_ID, NSFtpConfig.FTP_PORT,
//					NSFtpConfig.FTP_USER_NAME_YM,
//					NSFtpConfig.FTP_PASS_WORD_YM, 
//					"/4200.IMPBA.SWBEBTRADE.REPORT/out/",
//					file);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		//关闭fTPClient
//			fTPClient.disconnect();
//			System.out.println(fTPClient.isConnected());
		
//		//下载解析回执文件
		try {
//			Map<String, Object> map  =FtpUtil.downFile(NSFtpConfig.FTP_ID, 
//					NSFtpConfig.FTP_PORT, 
//					NSFtpConfig.FTP_USER_NAME_YM, 
//					NSFtpConfig.FTP_PASS_WORD_YM, "/4200.IMPBA.SWBEBTRADE.REPORT/out/", "", "f:/tools/download");
			Map<String, Object> map1  =FtpUtil.downFile(GZFtpConfig.FTP_ID, 
					GZFtpConfig.FTP_PORT, 
					GZFtpConfig.FTP_USER_NAME_YM, 
					GZFtpConfig.FTP_PASS_WORD_YM, "/out/", "", "f:/tools/download");
			String path ="";
			List pathList =(List) map1.get("path");
			for (int i = 0; i < pathList.size(); i++) {
				path=(String) pathList.get(i);
				File file =new File(path);
				try {
					Map<String, Object> resultMap=DOMXMLService.getHeadBeanList(file);
					System.out.println("resultMap--->"+resultMap);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
}
