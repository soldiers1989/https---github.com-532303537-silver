package org.silver.shop.impl.system.organization;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.organization.MemberService;
import org.silver.shop.dao.system.organization.MemberDao;
import org.silver.shop.model.system.commerce.GoodsContent;
import org.silver.shop.model.system.commerce.ShopCartContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.util.MD5;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass = MemberService.class)
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDao memberDao;

	@Override
	public Map<String, Object> memberRegister(String account, String loginPass, String memberIdCardName,
			String memberIdCard, String memberId) {
		Date date = new Date();
		Map<String, Object> statusMap = new HashMap<>();
		MD5 md = new MD5();
		Member member = new Member();
		member.setMemberId(memberId);
		member.setMemberName(account);
		member.setLoginPass(md.getMD5ofStr(loginPass));
		member.setMemberIdCard(memberIdCard);
		member.setMemberIdCardName(memberIdCardName);
		member.setCreateBy(account);
		member.setCreateDate(date);
		if (!memberDao.add(member)) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public List<Object> findMemberBy(String account) {
		Map<String, Object> params = new HashMap<>();
		params.put("memberName", account);
		return memberDao.findByProperty(Member.class, params, 0, 0);
	}

	@Override
	public Map<String, Object> createMemberId() {
		Map<String, Object> datasMap = new HashMap<>();
		Calendar cal = Calendar.getInstance();
		// 获取当前年份
		int year = cal.get(Calendar.YEAR);
		// 查询数据库字段名
		String property = "memberId";
		// 根据年份查询,当前年份下的id数量
		long memberIdCount = memberDao.findSerialNoCount(Member.class, property, year);
		// 当返回-1时,则查询数据库失败
		if (memberIdCount < 0) {
			datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return datasMap;
		}
		// 生成用户ID
		String memberId = SerialNoUtils.getSerialNotTimestamp("Member_", year, memberIdCount);
		datasMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
		datasMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
		datasMap.put(BaseCode.DATAS.getBaseCode(), memberId);
		return datasMap;
	}

	@Override
	public Map<String, Object> getMemberInfo(String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("memberName", memberName);
		List<Object> reList = memberDao.findByProperty(Member.class, params, 1, 1);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (reList.size() > 0) {
			Member member = (Member) reList.get(0);
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.getBaseCode(), member);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "用戶不存在！");
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> addGoodsToShopCart(String memberId, String memberName, String goodsId, int count) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("goodsId", goodsId);
		List<Object> reList = memberDao.findByProperty(GoodsContent.class, params, 1, 1);
		params.clear();
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (reList.size() > 0) {
			GoodsContent goods = (GoodsContent) reList.get(0);
			ShopCartContent shopCart = new ShopCartContent();
			params.put("goodsBaseId", goodsId);
			params.put("memberId", memberId);
			// 查询当前用户购物车中是否有该商品
			List<Object> reShopCart = memberDao.findByProperty(ShopCartContent.class, params, 1, 1);
			if (reShopCart == null) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			} else if (reShopCart.size() > 0) {
				ShopCartContent oldShopCart = (ShopCartContent) reShopCart.get(0);
				int oldCount = oldShopCart.getCount();
				int newCount = oldCount + count;
				oldShopCart.setCount(newCount);
				// 获取到原购物车中商品单价
				Double price = oldShopCart.getRegPrice();
				oldShopCart.setTotalPrice(newCount * price);
				if (!memberDao.update(oldShopCart)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
				return statusMap;
			}
			shopCart.setMemberId(memberId);
			shopCart.setMemberName(memberName);
			shopCart.setMerchantId(goods.getGoodsMerchantId());
			shopCart.setMerchantName(goods.getGoodsMerchantName());
			shopCart.setGoodsBaseId(goods.getGoodsId());
			shopCart.setGoodsName(goods.getGoodsName());
			shopCart.setGoodsImage(goods.getGoodsImage());
			shopCart.setGoodsStyle(goods.getGoodsStyle());
			shopCart.setCount(count);
			shopCart.setFlag(0);
			Double price = goods.getGoodsRegPrice();
			shopCart.setRegPrice(price);
			Double totalPrice = price * count;
			shopCart.setTotalPrice(totalPrice);
			if (!memberDao.add(shopCart)) {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
				return statusMap;
			}
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), "商品不存在不存在！");
			return statusMap;
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;

	}

	@Override
	public Map<String, Object> getGoodsToShopCartInfo(String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		List<Object> cartList = memberDao.findByProperty(ShopCartContent.class, params, 0, 0);
		if (cartList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (cartList.size() > 0) {
			for (int i = 0; i < cartList.size(); i++) {
				ShopCartContent cart = (ShopCartContent) cartList.get(i);
				String goodsId = cart.getGoodsBaseId();
				params.clear();
				params.put("goodsId", goodsId);
				// 根据商品ID查询库存中上架数量
				List<Object> stockList = memberDao.findByProperty(StockContent.class, params, 1, 1);
				if(stockList == null){
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
					return statusMap;
				}else if(stockList.size() >0){
					StockContent stock = (StockContent) stockList.get(0);
					cart.setSellCount(stock.getSellCount());
				}else{
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
					return statusMap;
				}
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.getBaseCode(), cartList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> deleteShopCartGoodsInfo(String goodsId, String memberId, String memberName) {
		Map<String,Object> statusMap = new HashMap<>();
		Map<String,Object> params = new HashMap<>();
		params.put("goodsBaseId", goodsId);
		params.put("memberId", memberId);
		List<Object> reList= memberDao.findByProperty(ShopCartContent.class, params, 1, 1);
		if(reList == null){
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		}else if(reList.size() > 0){
			ShopCartContent cart  = (ShopCartContent) reList.get(0);
			if(!memberDao.delete(cart)){
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		}else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> editShopCartGoodsFlag(String goodsId, String memberId, String memberName,int flag) {
		Map<String,Object> statusMap = new HashMap<>();
		Map<String,Object> params = new HashMap<>();
		params.put("goodsId", goodsId);
		params.put("memberId", memberId);
		List<Object> reList= memberDao.findByProperty(ShopCartContent.class, params, 1, 1);
		if(reList == null){
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		}else if(reList.size() > 0){
			ShopCartContent cart  = (ShopCartContent) reList.get(0);
			cart.setFlag(flag);
			if(!memberDao.update(cart)){
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		}else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

}
