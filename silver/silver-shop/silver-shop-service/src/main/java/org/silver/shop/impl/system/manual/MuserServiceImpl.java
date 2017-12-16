package org.silver.shop.impl.system.manual;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.silver.shop.api.system.manual.MuserService;
import org.silver.shop.dao.system.manual.MuserDao;
import org.silver.shop.model.system.manual.Muser;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=MuserService.class)
public class MuserServiceImpl implements MuserService{

	 @Resource
	 private MuserDao muserDao;
	
	
	@Override
	public Map<String, Object> addEntity(String merchant_no, String[] strs) {
		Map<String,Object> reqMap = new HashMap<>();
		if(merchant_no!=null&&strs!=null&&strs.length>=8){
			reqMap.put("muser_name", strs[0]);
			reqMap.put("muser_ID", strs[3]);
			reqMap.put("del_flag", 0);
			List<Muser> mul=muserDao.findByProperty(reqMap, 1, 1);
			if(mul!=null&&mul.size()>0){
				reqMap.put("status", -2);
				reqMap.put("msg", "用户【"+strs[0]+"】已存在，不需要重复添加");
				return reqMap;
			}
			List<Muser> last=muserDao.getLast();
			long id=0;
			if(last!=null&&last.size()>0){
				id=last.get(0).getId();
			}else if(last==null){
				reqMap.put("status", -5);
				reqMap.put("msg", "系统内部错误，请稍后重试");
				return reqMap;
			}
			String sysNo=createSysNo(id+1);
			Muser entity =new Muser();
			entity.setMerchant_no(merchant_no);
			entity.setMuser_sys_no(sysNo);
			entity.setMuser_name(strs[0]);
			entity.setMuser_tel(strs[1]);
			entity.setMuser_cer_type(strs[2]);
			entity.setMuser_ID(strs[3]);
			entity.setBank_type(strs[4]);
			entity.setBank_card_no(strs[5]);
			entity.setAdm_area_code(strs[6]);
			entity.setMuser_addr(strs[7]);
			
			entity.setDel_flag(0);
			entity.setCreate_date(new Date());
			entity.setCreate_by(merchant_no);
			if(muserDao.add(entity)){
				reqMap.put("status", 1);
				reqMap.put("msg", "信息录入成功");
				return reqMap;
			}
			reqMap.put("status", -1);
			reqMap.put("msg", "系统内部错误，信息录入失败，请稍后重试");
			return reqMap;
		}
		reqMap.put("status", -3);
		reqMap.put("msg", "非法数据，请填写完毕再试");
		return reqMap;
		
	}

	private String createSysNo(long id){
		String str = id+"";
		Random r = new Random();
		String m=r.nextInt(10000)+"";
		while(str.length()<7){
			str="0"+str;
		}
		while(m.length()<5){
			m="0"+m;
		}
		str="YMUR_"+str+m;
		return str;
	}

	@Override
	public Map<String, Object> loadMuserDatas(String merchant_no, int page, int size) {
		Map<String, Object> dataMap=new HashMap<>();
		dataMap.put("merchant_no", merchant_no);
		dataMap.put("del_flag", 0);
		List<Muser> mul=muserDao.findByProperty(dataMap,page,size);
		long count = muserDao.findByPropertyCount(dataMap);
		if(mul!=null&&mul.size()>0){
			dataMap.put("status", 1);
			dataMap.put("datas", mul);
			dataMap.put("count", count);
			return dataMap;
		}
		dataMap.put("status", -1);
		dataMap.put("count", count);
		return dataMap;
	}

	@Override
	public Map<String, Object> delMubySysno(String merchant_no, String muser_sys_no) {
		Map<String, Object> statusMap = new HashMap<>();
		if(merchant_no!=null&&!"".equals(merchant_no.trim())&&muser_sys_no!=null&&!"".equals(muser_sys_no.trim())){
			Map<String, Object> params = new HashMap<>();
			params.put("merchant_no", merchant_no);
			params.put("muser_sys_no", muser_sys_no);
			List<Muser> mul=muserDao.findByProperty(params, 1, 1);
			if(mul!=null&&mul.size()>0){
				if(muserDao.delete(mul.get(0))){
					statusMap.put("status", 1);
					statusMap.put("msg", "移除数据成功");
					return statusMap;
				}
				statusMap.put("status", -1);
				statusMap.put("msg", "移除数据出错，请重试");
				return statusMap;
			}
		}
		statusMap.put("status", -3);
		statusMap.put("msg", "非法请求");
		return statusMap;
	}
	
}
