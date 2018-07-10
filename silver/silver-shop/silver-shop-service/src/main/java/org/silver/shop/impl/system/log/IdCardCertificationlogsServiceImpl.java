package org.silver.shop.impl.system.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.shop.api.system.log.IdCardCertificationlogsService;
import org.silver.shop.dao.system.log.IdCardCertificationlogsDao;
import org.silver.shop.model.system.log.IdCardCertificationLog;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.model.system.manual.MorderSub;
import org.silver.shop.util.SearchUtils;
import org.silver.util.ReturnInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = IdCardCertificationlogsService.class)
public class IdCardCertificationlogsServiceImpl implements IdCardCertificationlogsService {

	@Autowired
	private IdCardCertificationlogsDao ikdCardCertificationlogsDao;
	
	@Override
	public Map<String, Object> getlogsInfo(Map<String, Object> datasMap, int page, int size) {
		Map<String,Object> reMap = SearchUtils.universalIdCardCertificationlogsSearch(datasMap);
		if(!"1".equals(reMap.get(BaseCode.STATUS.toString()))){
			return reMap;
		}
		Map<String, Object> paramMap = (Map<String, Object>) reMap.get("param");
		List<IdCardCertificationLog> idCardlist = ikdCardCertificationlogsDao.findByPropertyLike(IdCardCertificationLog.class, paramMap, null, page, size);
		long count = ikdCardCertificationlogsDao.findByPropertyLikeCount(IdCardCertificationLog.class, paramMap, null);
		if(idCardlist == null ){
			return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
		}else if (!idCardlist.isEmpty()) {
			return ReturnInfoUtils.successDataInfo(idCardlist, count);
		}else{
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
	}

}
