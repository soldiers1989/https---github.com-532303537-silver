package org.silver.shop.impl.system.organization;

import java.util.ArrayList;
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
import org.silver.shop.model.system.commerce.OrderContent;
import org.silver.shop.model.system.commerce.ShopCartContent;
import org.silver.shop.model.system.commerce.StockContent;
import org.silver.shop.model.system.organization.Member;
import org.silver.shop.model.system.tenant.MemberWalletContent;
import org.silver.util.MD5;
import org.silver.util.SerialNoUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.justep.baas.data.Row;
import com.justep.baas.data.Table;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
		MemberWalletContent wallet = new MemberWalletContent();
		wallet.setMemberId(memberId);
		wallet.setMemberName(account);
		wallet.setBalance(0.0);
		if (!memberDao.add(wallet)) {
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
		if (reList != null && reList.size() > 0) {
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
		// 根据前台传递的商品ID查询商品是否存在
		List<Object> goodsList = memberDao.findByProperty(GoodsContent.class, params, 1, 1);
		// 在查询库存中上架(售卖数量是够足够)
		List<Object> stockList = memberDao.findByProperty(StockContent.class, params, 1, 1);
		params.clear();
		StockContent stock = (StockContent) stockList.get(0);
		int reSellCount = stock.getSellCount();
		if (count > reSellCount) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.NOTICE.getStatus());
			statusMap.put(BaseCode.MSG.toString(), "库存不足,请重新输入");
			return statusMap;
		}
		if (goodsList != null && goodsList.size() > 0) {
			GoodsContent goods = (GoodsContent) goodsList.get(0);
			ShopCartContent shopCart = new ShopCartContent();
			params.put("goodsBaseId", goodsId);
			params.put("memberId", memberId);
			params.put("flag", 1);
			// 查询当前用户购物车中是否有该商品
			List<Object> reShopCart1 = memberDao.findByProperty(ShopCartContent.class, params, 1, 1);
			params.put("flag", 2);
			List<Object> reShopCart2 = memberDao.findByProperty(ShopCartContent.class, params, 1, 1);
			reShopCart1.addAll(reShopCart2);
			if (reShopCart1 != null && reShopCart1.size() > 0) {
				ShopCartContent oldShopCart = (ShopCartContent) reShopCart1.get(0);
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
			shopCart.setFlag(1);
			shopCart.setRegPrice(stock.getRegPrice());
			Double totalPrice = stock.getRegPrice() * count;
			shopCart.setTotalPrice(totalPrice);
			shopCart.setEntGoodsNo(stock.getEntGoodsNo());
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
		params.put("flag", 1);
		List<Object> cartList1 = memberDao.findByProperty(ShopCartContent.class, params, 0, 0);
		params.put("flag", 2);
		List<Object> cartList2 = memberDao.findByProperty(ShopCartContent.class, params, 0, 0);
		cartList1.addAll(cartList2);
		if (cartList1 != null && cartList1.size() > 0) {
			for (int i = 0; i < cartList1.size(); i++) {
				ShopCartContent cart = (ShopCartContent) cartList1.get(i);
				String goodsId = cart.getGoodsBaseId();
				params.clear();
				params.put("goodsId", goodsId);
				// 根据商品ID查询库存中上架数量
				List<Object> stockList = memberDao.findByProperty(StockContent.class, params, 1, 1);
				if (stockList != null && stockList.size() > 0) {
					StockContent stock = (StockContent) stockList.get(0);
					cart.setSellCount(stock.getSellCount());
				} else {
					statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
					statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
					return statusMap;
				}
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.getBaseCode(), cartList1);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	public Map<String, Object> deleteShopCartGoodsInfo(String goodsId, String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("goodsBaseId", goodsId);
		params.put("memberId", memberId);
		List<Object> reList = memberDao.findByProperty(ShopCartContent.class, params, 1, 1);
		if (reList == null) {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
			return statusMap;
		} else if (reList.size() > 0) {
			ShopCartContent cart = (ShopCartContent) reList.get(0);
			if (!memberDao.delete(cart)) {
				statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.WARN.getStatus());
				statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.WARN.getMsg());
				return statusMap;
			}
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.SUCCESS.getMsg());
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

	@Override
	// 临时使用
	public Map<String, Object> editShopCartGoodsFlag(String goodsInfoPack, String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		JSONArray jsonList = null;
		try {
			jsonList = JSONArray.fromObject(goodsInfoPack);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 将所有标识为2的购物车中的商品,修改为1
		params.put("flag", 2);
		List<Object> re = memberDao.findByProperty(ShopCartContent.class, params, 0, 0);
		 if (re !=null && re.size() > 0) {
			for (int i = 0; i < re.size(); i++) {
				ShopCartContent cart = (ShopCartContent) re.get(i);
				cart.setFlag(1);
				if (!memberDao.update(cart)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}
			}
		}

		for (int i = 0; i < jsonList.size(); i++) {
			Map<String, Object> reMap = (Map<String, Object>) jsonList.get(i);
			params.clear();
			params.put("goodsBaseId", reMap.get("goodsId"));
			params.put("memberId", memberId);
			List<Object> reList = memberDao.findByProperty(ShopCartContent.class, params, 1, 1);
			if (reList != null && reList.size() > 0) {
				ShopCartContent cart = (ShopCartContent) reList.get(0);
				cart.setCount(Integer.parseInt(reMap.get("count") + ""));
				cart.setFlag(2);
				if (!memberDao.update(cart)) {
					statusMap.put(BaseCode.STATUS.toString(), StatusCode.WARN.getStatus());
					statusMap.put(BaseCode.MSG.toString(), StatusCode.WARN.getMsg());
					return statusMap;
				}

			} else {
				statusMap.put(BaseCode.STATUS.toString(), StatusCode.NO_DATAS.getStatus());
				statusMap.put(BaseCode.MSG.toString(), StatusCode.NO_DATAS.getMsg());
				return statusMap;
			}
		}
		statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
		statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> getMemberOrderInfo(String memberId, String memberName, int page, int size) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("memberName", memberName);
		long totalCount = memberDao.findByPropertyCount(OrderContent.class, params);
		Table table = memberDao.findOrderInfo(memberId, page, size);

		if (table != null && table.getRows().size() > 0) {
			Map<String, List<Map<String, Object>>> orderMap = new HashMap<>();
			List<Row> lr = table.getRows();
			for (int i = 0; i < lr.size(); i++) {
				String orderId = lr.get(i).getValue("orderId") + "";
				String goodsId = lr.get(i).getValue("goodsId") + "";
				String goodsName = lr.get(i).getValue("goodsName") + "";
				String goodsImage = lr.get(i).getValue("goodsImage") + "";
				String goodsPrice = lr.get(i).getValue("goodsPrice") + "";
				String goodsCount = lr.get(i).getValue("goodsCount") + "";
				String goodsTotalPrice = lr.get(i).getValue("goodsTotalPrice") + "";
				String merchantId = lr.get(i).getValue("merchantId") + "";
				String merchantName = lr.get(i).getValue("merchantName") + "";
				String reMemberId = lr.get(i).getValue("memberId") + "";
				String reMemberName = lr.get(i).getValue("memberName") + "";
				String status = lr.get(i).getValue("status") + "";
				String createDate = lr.get(i).getValue("createDate") + "";
				if (orderMap != null && orderMap.get(orderId) != null) {
					Map<String, Object> goodsMap = new HashMap<>();

					goodsMap.put("goodsId", goodsId);
					goodsMap.put("goodsName", goodsName);
					goodsMap.put("goodsImage", goodsImage);
					goodsMap.put("goodsPrice", goodsPrice);
					goodsMap.put("goodsCount", goodsCount);
					goodsMap.put("goodsTotalPrice", goodsTotalPrice);
					goodsMap.put("merchantId", merchantId);
					goodsMap.put("merchantName", merchantName);
					goodsMap.put("MemberId", reMemberId);
					goodsMap.put("MemberName", reMemberName);
					goodsMap.put("merchantName", merchantName);
					goodsMap.put("createDate", createDate);
					goodsMap.put("status", status);
					orderMap.get(orderId).add(goodsMap);
				} else {
					Map<String, Object> goodsMap = new HashMap<>();
					goodsMap.put("goodsId", goodsId);
					goodsMap.put("goodsName", goodsName);
					goodsMap.put("goodsImage", goodsImage);
					goodsMap.put("goodsPrice", goodsPrice);
					goodsMap.put("goodsCount", goodsCount);
					goodsMap.put("goodsTotalPrice", goodsTotalPrice);
					goodsMap.put("merchantId", merchantId);
					goodsMap.put("merchantName", merchantName);
					goodsMap.put("reMemberId", reMemberId);
					goodsMap.put("reMemberName", reMemberName);
					goodsMap.put("merchantName", merchantName);
					goodsMap.put("createDate", createDate);
					goodsMap.put("status", status);
					List<Map<String, Object>> list = new ArrayList<>();
					list.add(goodsMap);
					orderMap.put(orderId, list);

				}
			}
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), orderMap);
			statusMap.put(BaseCode.TOTALCOUNT.toString(), totalCount);
			return statusMap;

		}
		statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
		statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
		return statusMap;
	}

	@Override
	public Map<String, Object> getMemberWalletInfo(String memberId, String memberName) {
		Map<String, Object> statusMap = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		params.put("memberId", memberId);
		params.put("memberName", memberName);
		List<Object> reList = memberDao.findByProperty(MemberWalletContent.class, params, 0, 0);
		if (reList != null && reList.size() > 0) {
			statusMap.put(BaseCode.STATUS.toString(), StatusCode.SUCCESS.getStatus());
			statusMap.put(BaseCode.MSG.toString(), StatusCode.SUCCESS.getMsg());
			statusMap.put(BaseCode.DATAS.toString(), reList);
			return statusMap;
		} else {
			statusMap.put(BaseCode.STATUS.getBaseCode(), StatusCode.NO_DATAS.getStatus());
			statusMap.put(BaseCode.MSG.getBaseCode(), StatusCode.NO_DATAS.getMsg());
			return statusMap;
		}
	}

}
