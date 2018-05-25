package org.silver.shop.controller.common.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silver.shop.service.common.base.PostalTransaction;
import org.silver.util.DateUtil;
import org.silver.util.JedisUtil;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.SerializeUtil;
import org.silver.wdFilter.TestSensitiveWdFilter;
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
	 * (加载)读取敏感字符文本
	 * 
	 * @return String
	 */
	@RequestMapping(value = "/getContent", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	@ApiOperation("查询敏感字符文本")
	public String getContent(HttpServletRequest req, HttpServletResponse response, @RequestParam("page") int page,
			@RequestParam("size") int size) {
		String originHeader = req.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, accept, content-type, xxxx");
		response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Origin", originHeader);
		List<String> words = null;
		byte[] redisByte = JedisUtil.get(SHOP_KEY_SENSITIVE_WORDS_LIST.getBytes());
		if (redisByte != null) {
			words = (List<String>) SerializeUtil.toObject(redisByte);
		} else {
			words = WordFilter.readWordFromFile("wd.txt");
			// 将数据放入到缓存中
			JedisUtil.set(SHOP_KEY_SENSITIVE_WORDS_LIST.getBytes(), SerializeUtil.toBytes(words), 3600 * 24);
		}
		List<String> newWords = new ArrayList<>();
		int currIdx = (page > 1 ? (page - 1) * size : 0);
		for (int i = 0; i < size && i < words.size() - currIdx; i++) {
			String word = words.get(currIdx + i);
			newWords.add(word);
		}
		return JSONObject.fromObject(ReturnInfoUtils.successDataInfo(newWords, words.size())).toString();
	}

	/**
	 * 删除敏感字符文本
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

		return JSONObject.fromObject(ReturnInfoUtils.successInfo()).toString();
	}

	public static void main(String[] args) {
		BufferedReader br;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new FileReader(new File("C:\\Users\\Lenovo\\Desktop\\Test.txt")));
			String temp = "";
			while ((temp = br.readLine()) != null) {
				System.out.println("--->>>" + temp);
				if(temp.trim().equals(";")){
					continue;
				}
				 sb.append(temp+"\n");
			}
			write(sb.toString());
			  br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void write(String fixedText) {
        BufferedWriter bw=null;
        try {
            bw=new BufferedWriter(new FileWriter(new File("C:\\Users\\Lenovo\\Desktop\\newTest.txt")));
            bw.write(fixedText);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
