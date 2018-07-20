package org.silver.shop.impl.common.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.common.base.IdCardService;
import org.silver.shop.dao.common.base.IdCardDao;
import org.silver.shop.model.common.base.IdCard;
import org.silver.shop.model.system.manual.Morder;
import org.silver.shop.util.SearchUtils;
import org.silver.util.DateUtil;
import org.silver.util.IdcardValidator;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.silver.util.YmHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.mysql.fabric.xmlrpc.base.Array;

@Service(interfaceClass = IdCardService.class)
public class IdCardServiceImpl implements IdCardService {

	@Autowired
	private IdCardDao idCardDao;

	@Override
	public Map<String, Object> getAllIdCard(int page, int size, Map<String, Object> datasMap) {
		if (page >= 0 && size >= 0) {
			Map<String, Object> reDatasMap = SearchUtils.universalIdCardSearch(datasMap);
			if (!"1".equals(reDatasMap.get(BaseCode.STATUS.toString()))) {
				return reDatasMap;
			}
			Map<String, Object> paramMap = (Map<String, Object>) reDatasMap.get("param");
			List<IdCard> idList = idCardDao.findByPropertyLike(IdCard.class, paramMap, null,page, size);
			long count = idCardDao.findByPropertyLikeCount(IdCard.class, paramMap,null);
			if (idList == null) {
				return ReturnInfoUtils.errorInfo("查询失败,服务器繁忙!");
			} else if (!idList.isEmpty()) {
				return ReturnInfoUtils.successDataInfo(idList, count);
			}
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
		return ReturnInfoUtils.errorInfo("请求参数错误!");
	}

	@Override
	public Map<String, Object> editIdCardInfo(long id, String idName, String idNumber, int type) {
		Map<String, Object> params = new HashMap<>();
		if (id > 0 && StringEmptyUtils.isNotEmpty(idName) && StringEmptyUtils.isNotEmpty(idNumber) && type > 0) {
			params.put("id", id);
			List<IdCard> idList = idCardDao.findByProperty(IdCard.class, params, 1, 1);
			if (idList != null && !idList.isEmpty()) {
				IdCard idCard = idList.get(0);
				idCard.setIdNumber(idNumber);
				idCard.setName(idName);
				// 类型：1-未验证,2-手工验证,3-海关认证,4-第三方认证,5-错误
				idCard.setType(type);
				if (!idCardDao.update(idCard)) {
					return ReturnInfoUtils.errorInfo("修改身份证信息失败,服务器繁忙!");
				}
				return ReturnInfoUtils.successInfo();
			} else {
				return ReturnInfoUtils.errorInfo("身份证未找到对应信息,请核对信息!");
			}
		}
		return ReturnInfoUtils.errorInfo("请求参数出错,请重试!");
	}

	@Override
	public Map<String, Object> deleteDuplicateIdCardInfo() {
		Map<String, Object> params = new HashMap<>();
		params.put("startDate", DateUtil.parseDate("2018-07-02 11:52:13", "yyyy-MM-dd HH:mm:ss"));
		params.put("endDate", new Date());
		List<IdCard> idCardList = idCardDao.findByPropertyLike(IdCard.class, params, null, 0, 0);
		if (idCardList != null && !idCardList.isEmpty()) {
			System.out.println("--共计-->>>>" + idCardList.size());
			Map<String, Object> chacheMap = new HashMap<>();
			for (IdCard idCard : idCardList) {
				if (chacheMap
						.containsKey(idCard.getName() + "_" + idCard.getIdNumber() + "_" + idCard.getMerchantId())) {
					if (idCardDao.delete(idCard)) {
						System.out.println("---重名-删除->" + idCard.getName() + "_" + idCard.getIdNumber());
					}
				} else {
					chacheMap.put(idCard.getName() + "_" + idCard.getIdNumber() + "_" + idCard.getMerchantId(), "");

				}
			}
		}
		return null;
	}

	@Override
	public Object temPush() {
		//
		int page = 100;
		int size = 500;
		List<IdCard> idcardList = idCardDao.findByProperty(IdCard.class, null, page, size);
		while (idcardList != null && !idcardList.isEmpty()) {
			if (page != 1) {
				idcardList = idCardDao.findByProperty(IdCard.class, null, page, size);
			}
			Map<String, Object> item = new HashMap<>();
			long startTime = System.currentTimeMillis();
			for (IdCard idCard : idcardList) {
				item.put("user_name", idCard.getName());
				item.put("user_ID", idCard.getIdNumber());
				System.out.println(
						"-0---->>" + YmHttpUtil.HttpPost("http://192.168.1.172:8080/silver-web/real/addInfo", item));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			page++;
			long endTime = System.currentTimeMillis();
			System.out.println("---一次循环耗时->>>" + (endTime - startTime) + "ms");
			System.out.println(page + "<<页数----");
		}
		return null;
	}
}
