package org.silver.shop.impl.system.organization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.ManagerService;
import org.silver.shop.dao.system.organization.ManagerDao;
import org.silver.shop.model.system.organization.Manager;
import org.silver.shop.model.system.organization.Member;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = ManagerService.class)
public class ManagerServiceImpl implements ManagerService {

	@Autowired
	private ManagerDao managerDao;
	
	@Override
	public List<Object> findManagerBy(String account) {
		Map<String, Object> params = new HashMap<>();
		params.put("managerName", account);
		return managerDao.findByProperty(Manager.class, params, 0, 0);
	}

	@Override
	public Map<String, Object> findAllmemberInfo() {
		Map<String,Object> statusMap = new HashMap<>();
		Map<String,Object> paramMap = new HashMap<>();
		List<Object> reList = managerDao.findByProperty(Member.class, paramMap, 0, 0);
		if(reList ==null){
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}else if(!reList.isEmpty()){
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		}else{
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

}
