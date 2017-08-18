package org.silver.util;
/**
 * 名称：http请求类
 * 功能：实现http的POST/GET操作
 * 作者：何明发
 * 时间：2013-10-26
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;





public class YmHttpUtil {		
	//***********微信测试***************
	private static final String APPID="wxd6a78b74d5aa02a7";
	private static final String APPSECRET="8ae3b4e3cb6180571a23efcd226d2b9e";
	private static final String TOKEN_URL= "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	
	
	//***********HTTP后台访问**********
	private static final int ConnectTimeout=10000;
	private static final int ReadTimeout=10000;
	private static Logger logger=Logger.getLogger(org.silver.util.YmHttpUtil.class);
	/**
	 * 创建Http的post请求并返回结果
	 * @param url
	 * @param params
	 * @param charset
	 * @return
	 */
	public static String HttpPost(String url,Map<String,Object> params){
		return HttpPost(url,params,"utf-8");
	}
	public static String HttpPost(String url,Map<String,Object> params,String charset){
		URL u=null;
		HttpURLConnection con=null;
		String ssb="";
		String outStr="";
		//构造参数
		StringBuffer sb = new StringBuffer();
		if(params!=null){
			try{
				for (Entry<String, Object> e : params.entrySet()) {				
					String type=e.getValue().getClass().toString();
					if("class java.util.ArrayList".equals(type)){
						List templist=(List)e.getValue();
						if(templist.size()>0){
							for(int i=0;i<templist.size();i++){
								sb.append(e.getKey());
								sb.append("=");
								sb.append(URLEncoder.encode(templist.get(i).toString(),charset));
								sb.append("&");
							}
						}
					}else{				
						sb.append(e.getKey());
						sb.append("=");
						sb.append(URLEncoder.encode(e.getValue().toString(),charset));
						sb.append("&");
					}
					
				}
				ssb=sb.toString().trim();
			}catch(Exception e){
				ssb=null;
			}
			if(ssb!=null && ssb.length()>=1){
				outStr=ssb.substring(0, ssb.length()-1);
			}
		}
		//发送请求
		try {
			u = new URL(url);
			con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setConnectTimeout(ConnectTimeout);
			con.setReadTimeout(ReadTimeout);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(),charset);
			osw.write(outStr);
			osw.flush();
			osw.close();
		}  catch (Exception e) {
			logger.error("请求出错",e);
			return null;
		} finally {
			if (con != null) {
			con.disconnect();
			}
		}
		//读取返回内容		
		try {
			StringBuffer buffer = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
			String temp;
			while ((temp = br.readLine()) != null) {
				buffer.append(temp);
				buffer.append("\n");
			}
			return buffer.toString();
		} catch (SocketTimeoutException ste) {
			logger.error("访问超时");
			return null;
		} catch (Exception e) {
			logger.error("请求出错",e);
			return null;
		}		
	}
	
	/**
	 * 创建Http的Get请求并返回结果
	 * @param url
	 * @param params
	 * @param charset
	 * @return
	 */
	public static String HttpGet(String url,Map<String,Object> params){
		return HttpGet(url,params,"utf-8");
	}
	public static String HttpGet(String url,Map<String,Object> params,String charset){		
		URL u=null;
		HttpURLConnection con=null;
		String ssb="";
		String outStr="";
		//构造参数
		StringBuffer sb = new StringBuffer();
		if(params!=null){
			sb.append("?");
			for (Entry<String, Object> e : params.entrySet()) {
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				sb.append("&");
				//sb.substring(0, sb.length() - 1);
			}
			ssb=sb.toString().trim();
			outStr=ssb.substring(0, ssb.length()-1);
		}
		//System.out.println("url:"+url+outStr);
		//System.out.println("params:"+outStr);
		//发送请求
		try {
			u = new URL(url+outStr);
			con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setConnectTimeout(ConnectTimeout);
			con.setReadTimeout(ReadTimeout);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(),charset);
			osw.write("");
			osw.flush();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
			con.disconnect();
			}
		}
		//读取返回内容
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
			String temp;
			while ((temp = br.readLine()) != null) {
				buffer.append(temp);
				buffer.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		 
		return buffer.toString();	
	}
	//*********Http前端跳转**********
	/**
	 * 自动跳转
	 * @param url
	 * @param params
	 * @param month
	 * @return
	 */
	public static String autoJump(String url,Map<String,Object> params,String month){
		StringBuffer html=new StringBuffer();
		html.append("<form id=\"codehtml\" name=\"codehtml\" action=\"" + url
                + "\" method=\""+month+"\" >");
		if(params!=null){
			for (Entry<String, Object> e : params.entrySet()) {
				html.append("<input type=\"hidden\" name=\""+e.getKey()+"\" value=\"" + e.getValue() + "\"/>");
				//sb.substring(0, sb.length() - 1);
			}
		}
		html.append("<input type=\"submit\" value=\"确认\" style=\"display:none;\"></form>");
		html.append("<script>document.forms['codehtml'].submit();</script>");
		return html.toString();
	}
	/**
	 * 手动跳转
	 * @param url
	 * @param params
	 * @param month
	 * @return
	 */
	public static String manuJump(String url,Map<String,String> params,String month){
		StringBuffer html=new StringBuffer();
		html.append("<form id=\"codehtml\" name=\"codehtml\" action=\"" + url
                + "\" method=\""+month+"\" >");
		if(params!=null){
			for (Entry<String, String> e : params.entrySet()) {
				html.append("<input type=\"hidden\" name=\""+e.getKey()+"\" value=\"" + e.getValue() + "\"/>");
				//sb.substring(0, sb.length() - 1);
			}
		}
		html.append("<input type=\"submit\" value=\"确认\"></form>");		
		return html.toString();
	}
	
	/**
	 * 解析URL里面的参数
	 * @param strURL
	 * @return map
	 */
	public static Map<String,Object> getParam(String strURL) 
	{ 
		String strAllParam=null,url=strURL; 
		String[] arrSplit=null; 
		String[] arrParam=null;
		Map<String,Object> map=new HashMap<String,Object>();
		strURL=strURL.trim(); 
		arrSplit=strURL.split("[?]");
		if(strURL.length()>1) 
		{ 
			if(arrSplit.length>1) 
			{ 
				if(arrSplit[1]!=null) 
				{ 
					url=arrSplit[0];
					strAllParam=arrSplit[1]; 
				} 
				arrParam=strAllParam.split("&");
			} 		
		} 		
		if(arrParam!=null){			
			for(int i=0;i<arrParam.length;i++){
				map.put(arrParam[i].split("=")[0],arrParam[i].split("=")[1]);
			}
		}
		map.put("URL", url);
		//System.out.println("url分解结果："+map);
		return map; 
	}
	public static String getNewUrl(String url,Map<String,Object> params){		
		if(url!=null && !"".equals(url) && params!=null){
			StringBuffer newUrl=new StringBuffer();
			newUrl.append(url);
			int row=0;
			for (Entry<String, Object> e : params.entrySet()) {
				if(row==0){
					newUrl.append("?"+e.getKey()+"=" + e.getValue());
				}else{
					newUrl.append("&"+e.getKey()+"=" + e.getValue());
				}
				row++;
			}
			//System.out.println("URL组装结果："+newUrl);
			return newUrl.toString();
		}
		return "";
	}
	public static String appendParam(String url,Map<String,Object> params){
		if(url!=null && !url.isEmpty() && params!=null){
			Map<String,Object> param=getParam(url);
			Object o=param.get("URL");
			if(o==null){
				return null;
			}
			String nurl=o.toString();
			param.remove("URL");
			for (Entry<String, Object> e : params.entrySet()) {
				try{
					String[] temp=(String[])params.get(e.getKey());
					params.put(e.getKey(), temp[0]);
				}catch(ClassCastException ce){
					String temp=(String)params.get(e.getKey());
					params.put(e.getKey(), temp);
				}
				
			}
			if(params!=null && params.size()>0){
				param.putAll(params);
			}
			String newurl=getNewUrl(nurl,param);
			return newurl;
		}
		return null;
	}
	//
	public static String getHostName(String url){
		if(url!=null && !url.isEmpty()){
			int start=url.indexOf("//");
			if(start<0){
				start=0;
			}else{
				start=start+2;
			}
			int paramStart=url.indexOf("?",start);
			int stop=url.indexOf(":",start);
			if(stop<0 || stop>paramStart){
				stop=url.indexOf("/", start);
				if(stop<0){
					stop=url.length();
				}
			}
			return url.substring(start, stop);
		}
		return null;
	}
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args){
	
	}

}
