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
import org.silver.util.IdcardValidator;
import org.silver.util.ReturnInfoUtils;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.mysql.fabric.xmlrpc.base.Array;

@Service(interfaceClass = IdCardService.class)
public class IdCardServiceImpl implements IdCardService {

	@Autowired
	private IdCardDao idCardDao;

	@Override
	public Map<String, Object> getAllIdCard(String idName, String idNumber, int page, int size, String type) {
		if (page >= 0 && size >= 0) {
			Map<String, Object> params = new HashMap<>();
			if (StringEmptyUtils.isNotEmpty(idName)) {
				params.put("name", idName);
			}
			if (StringEmptyUtils.isNotEmpty(idNumber)) {
				params.put("idNumber", idNumber);
			}
			if (StringEmptyUtils.isNotEmpty(type)) {
				params.put("type", Integer.parseInt(type));
			}
			List<IdCard> idList = idCardDao.findByProperty(IdCard.class, params, page, size);
			long count = idCardDao.findByPropertyCount(IdCard.class, params);
			if (idList != null && !idList.isEmpty()) {
				params.clear();
				params.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				params.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
				params.put(BaseCode.TOTALCOUNT.toString(), count);
				params.put(BaseCode.DATAS.toString(), idList);
				return params;
			}
			return ReturnInfoUtils.errorInfo("暂无数据!");
		}
		return ReturnInfoUtils.errorInfo("请求参数出错,请重试!");
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
	public Map<String, Object> firstUpdateIdCardInfo() {
		int orderPage = 1;
		int orderSize = 200;
		Map<String, Object> params = new HashMap<>();
		// 只有订单备案成功后,才导入进实名库
		params.put("order_record_status", 3);
		List<Morder> morderList = idCardDao.findByProperty(Morder.class, params, 1, 200);
		while (morderList != null && !morderList.isEmpty() ) {
			if (orderPage != 1) {
				params.clear();
				morderList = idCardDao.findByProperty(Morder.class, params, orderPage, orderSize);
			}
			if (!morderList.isEmpty()) {
				for (Morder order : morderList) {
					String idNumber = order.getOrderDocId();
					params.clear();
					params.put("idNumber", order.getOrderDocId());
					List<IdCard> idCardList = idCardDao.findByProperty(IdCard.class, params, 0, 0);
					if (idCardList != null && idCardList.isEmpty()) {
						IdCard idCard = new IdCard();
						idCard.setName(order.getOrderDocName());
						idCard.setIdNumber(idNumber);
						// 类型：1-未验证,2-手工验证,3-海关认证,4-第三方认证,5-错误
						if (IdcardValidator.validate18Idcard(idNumber)) {
							idCard.setType(3);
						} else {
							idCard.setType(5);
						}
						idCard.setCreateBy("system");
						idCard.setCreateDate(new Date());
						idCard.setDeleteFlag(0);
						if(idCardDao.add(idCard)){
							System.out.println(orderPage+"<<页数-------身份证信息保存成功---------");
						}
					}else {
						System.out.println(orderPage+"<<页数-------身份证已存在无需重复导入---------");
					}
				}
			}
			orderPage++;
			System.out.println(orderPage+"<<页数----");
		}
		return ReturnInfoUtils.errorInfo("暂无数据!");
	}
}
