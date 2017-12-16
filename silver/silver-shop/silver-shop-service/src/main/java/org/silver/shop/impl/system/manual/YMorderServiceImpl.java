package org.silver.shop.impl.system.manual;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.silver.shop.api.system.manual.YMWalletService;
import org.silver.shop.api.system.manual.YMorderService;
import org.silver.shop.dao.system.manual.YMorderDao;
import org.silver.shop.model.system.manual.YMorder;
import org.silver.util.JedisUtil;
import org.silver.util.YmHttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Reference;

import com.alibaba.dubbo.config.annotation.Service;

import redis.clients.jedis.Jedis;


//银盟电子订单 下单服务
@Service(interfaceClass=YMorderService.class)
public class YMorderServiceImpl implements YMorderService{

	//@Autowired
	//private AppkeyService appkeyService;
	
	@Autowired
	private YMWalletService yMWalletService;
	
	@Resource
	private YMorderDao yMorderDao = new YMorderDao();
	
	private Logger logger = LoggerFactory.getLogger(getClass());
     	
	@Override
	public Map<String, Object> doBusiness(String merchant_no, String out_trade_no, String amount, String notify_url,
			String extra_common_param, String client_sign, String timestamp) {
		/**
		 * 查找appkey
		 */
		String str=merchant_no+out_trade_no+amount+notify_url+timestamp;
		//Map<String, Object> checkMap=appkeyService.CheckClientSign("4a5de70025a7425dabeef6e8ea752976", client_sign, str, timestamp);
		//if((int)checkMap.get("status")!=1){
		//	return checkMap;
		//}
		return createEntity(merchant_no, out_trade_no, amount, "content", extra_common_param, notify_url);
	}

	private Map<String,Object> createEntity(String merchant_no,String out_trade_no,String amount,String content,String extra_common_param,String notify_url){
		Map<String,Object> statusMap=new HashMap<>();
		double total=0;
		try{
			total=Double.parseDouble(amount);
			if(total<0.01){
				statusMap.put("status", -6);
				statusMap.put("msg", "无效的交易金额");
				return statusMap;
			}
		}catch (Exception e){
			statusMap.put("status", -4);
			statusMap.put("msg", "错误的交易金额");
			return statusMap;
		}
		
		List<YMorder> last=yMorderDao.getLast();
		long id=0;
		if(last!=null&&last.size()>0){
			id=last.get(0).getId();
		}else if(last==null){
			statusMap.put("status", -5);
			statusMap.put("msg", "系统内部错误，请稍后重试");
			return statusMap;
		}
		Date d =new Date();
		YMorder entity=new YMorder();
		entity.setOrder_id(createSysNo(id,d));
		entity.setMerchant_no(merchant_no);
		entity.setOut_trade_no(out_trade_no);
		entity.setAmount(total);
		entity.setContent(content);
		entity.setExtra_common_param(extra_common_param);
		entity.setNotify_url(notify_url);
		entity.setType(0);
		entity.setDel_flag(0);
		entity.setCreate_date(d);
		entity.setCreate_by(merchant_no);
		if(yMorderDao.add(entity)){
			statusMap.put("status", 1);
			statusMap.put("order_id", entity.getOrder_id());
			statusMap.put("msg", "订单生成成功");
			return statusMap;
		}
		statusMap.put("status", -1);
		statusMap.put("msg", "系统内部错误，请稍后重试");
		return statusMap;
		
	}
	private String createSysNo(long id,Date date){
		SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
		
		String str = id+"";
		Random r = new Random();
		String m=r.nextInt(10000)+"";
		while(str.length()<7){
			str="0"+str;
		}
		while(m.length()<5){
			m="0"+m;
		}
		str="YMOD_"+sdf.format(date)+str+m;
		return str;
	}
	
	public static void main(String[] args) {
		YMorderServiceImpl yms =new YMorderServiceImpl();
		Random r = new Random();
		while(true){
			System.out.println(yms.createSysNo(r.nextInt(10000000), new Date()));
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public Map<String, Object> doCallBack(String order_id, String trade_no, String trade_status) {
		Map<String, Object> params =new HashMap<>();
		params.put("order_id", order_id);
		List<YMorder> l = yMorderDao.findByProperty(params, 1, 1);
		if(l!=null&&l.size()>0){
			params.put("trade_no", trade_no);
			params.put("out_trade_no", l.get(0).getOut_trade_no());
			params.put("trade_status", trade_status);
			asyncNoticeClient(l.get(0).getNotify_url(), params);
			if(trade_status!=null&&trade_status.equals("TRADE_SUCCESS")){
				l.get(0).setOrder_status(1);
				l.get(0).setTrade_no(trade_no);
				l.get(0).setUpdate_date(new Date());
				l.get(0).setUpdate_by(trade_no);
				if(yMorderDao.update(l.get(0))){
					Map<String,Object> walletMap=yMWalletService.commUpdateMoney(l.get(0).getMerchant_no(), l.get(0).getAmount(), "银盛支付交易处理");
					params.put("status", 1);
					params.put("msg", "订单已更改为已支付");
					return params;
				}
				logger.error(order_id+"【"+trade_no+"】"+"："+trade_status+new Date()+"更新订单失败");
				JedisUtil.set(order_id+"_"+trade_status, 60*60*24*30, trade_no);
				params.put("status", -1);
				params.put("msg", "订单更改状态出错");
				return params;
				
			}
			l.get(0).setOrder_status(2);
			l.get(0).setUpdate_date(new Date());
			if(yMorderDao.update(l.get(0))){
				params.put("status", 1);
				params.put("msg", "订单已更改为支付失败");
				return params;
			}
			logger.error(order_id+"【"+trade_no+"】"+"："+trade_status+new Date()+"更新订单失败");
			JedisUtil.set(order_id+"_"+trade_status, 60*60*24*30, trade_no);
			params.put("status", -1);
			params.put("msg", "订单更改状态出错");
			return params;
			
		}
		params.put("status", -5);
		params.put("msg", "系统内部错误");
		return params;
	}
	
	private boolean asyncNoticeClient(String notifyUrl,Map params){
		String result=YmHttpUtil.HttpPost(notifyUrl, params);
		System.out.println("异步通知递四方"+result);
		if(result!=null){
			return true;
		}
		return false;
		
	}
}
