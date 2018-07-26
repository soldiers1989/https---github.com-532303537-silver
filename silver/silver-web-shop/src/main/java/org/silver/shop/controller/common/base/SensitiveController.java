package org.silver.shop.controller.common.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.silver.util.StringEmptyUtils;
import org.silver.wdFilter.WordFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * 
 */
@Controller
@RequestMapping("/sensitive")
public class SensitiveController {

	/**
	 * 商城敏感字缓存Key
	 */
	private static final String SHOP_KEY_SENSITIVE_WORDS_LIST = "Shop_Key_Sensitive_Words_List";

	/**
	 * 缓存时间为一个星期
	 */
	private static final int CACHE_TIME = 3600 * 24 * 7;
	/**
	 * (加载)读取敏感字符文本
	 * 
	 * @return String
	 */
	@RequestMapping(value = "/getContent", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询敏感字符文本")
	public String getContent(HttpServletRequest req, HttpServletResponse response, @RequestParam("page") int page,
			@RequestParam("size") int size, String content) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		List<String> words = reRedisInfo();
		if(words == null || words.isEmpty()){
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!")).toString();
		}
		List<String> newWords = new ArrayList<>();
		List<String> descWords = descList(words,content);
		if(descWords.isEmpty()){
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!")).toString();
		}
		int currIdx = (page > 1 ? (page - 1) * size : 0);
		for (int i = 0; i < size && i < descWords.size() - currIdx; i++) {
			String word = descWords.get(currIdx + i);
			newWords.add(word);
		}
		return JSONObject.fromObject(ReturnInfoUtils.successDataInfo(newWords, descWords.size())).toString();
	}
	
	/**
	 * 倒叙集合中的参数
	 * @param words 敏感字-字符串
	 * @param content 搜索关键字
	 * @return List<String>
	 */
	private List<String> descList(List<String> words, String content) {
		List<String> descWords = new ArrayList<>();
		if (StringEmptyUtils.isNotEmpty(content)) {
			for (int i = (words.size() - 1); i > 0; i--) {
				String str = words.get(i);
				if(str.trim().contains(content)){
					descWords.add(str);
				}
			}
		}else{
			for (int i = (words.size() - 1); i > 0; i--) {
				String str = words.get(i);
				descWords.add(str);
			}
		}
		return descWords;
	}

	/**
	 * 临时---删除（缓存中）敏感字符文本
	 * 
	 * @return String
	 */
	@RequestMapping(value = "/deleteContent", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("删除敏感字符文本")
	public String deleteContent(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("content") String content) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		List<String> words = reRedisInfo();
		if (words == null || words.isEmpty() || StringEmptyUtils.isEmpty(content)) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("删除失败,服务器繁忙!")).toString();
		}
		List<String> newWords = new ArrayList<>();
		for (String tmp : words) {
			if (StringEmptyUtils.isNotEmpty(tmp) && tmp.trim().equals(content)) {
				continue;
			}
			newWords.add(tmp);
		}
		// 更新缓存
		JedisUtil.set(SHOP_KEY_SENSITIVE_WORDS_LIST.getBytes(), SerializeUtil.toBytes(newWords), CACHE_TIME);
		return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
	}

	/**
	 * 临时---增加（缓存中）敏感字符文本
	 * 
	 * @return String
	 */
	@RequestMapping(value = "/addContent", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("增加敏感字符文本")
	public String addContent(HttpServletRequest req, HttpServletResponse response,
			@RequestParam("content") String content) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		List<String> words = reRedisInfo();
		if (words == null || words.isEmpty()) {
			return JSONObject.fromObject(ReturnInfoUtils.errorInfo("添加失败,服务器繁忙!")).toString();
		}
		words.add(content);
		// 更新缓存
		JedisUtil.set(SHOP_KEY_SENSITIVE_WORDS_LIST.getBytes(), SerializeUtil.toBytes(words), CACHE_TIME);
		return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
	}

	/**
	 * 获取缓存中敏感字符文本
	 * 
	 * @return List<String> 文本集合
	 */
	private List<String> reRedisInfo() {
		List<String> words = null;
		byte[] redisByte = JedisUtil.get(SHOP_KEY_SENSITIVE_WORDS_LIST.getBytes());
		if (redisByte != null) {
			words = (List<String>) SerializeUtil.toObject(redisByte);
		} else {
			words = WordFilter.readWordFromFile("wd.txt");
			// 将数据放入到缓存中
			JedisUtil.set(SHOP_KEY_SENSITIVE_WORDS_LIST.getBytes(), SerializeUtil.toBytes(words), CACHE_TIME);
		}
		return words;
	}

	public static void main(String[] args) {
		BufferedReader br;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new FileReader(new File("C:\\Users\\Lenovo\\Desktop\\Test.txt")));
			String temp = "";
			while ((temp = br.readLine()) != null) {
				System.out.println("--->>>" + temp);
				if (temp.trim().equals(";")) {
					continue;
				}
				sb.append(temp + "\n");
			}
			write(sb.toString());
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void write(String fixedText) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File("C:\\Users\\Lenovo\\Desktop\\newTest.txt")));
			bw.write(fixedText);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
