package org.silver.shop.impl.system.tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.silver.common.BaseCode;
import org.silver.common.StatusCode;
import org.silver.shop.api.system.tenant.WalletLogService;
import org.silver.shop.dao.system.tenant.WalletLogDao;
import org.silver.shop.model.system.log.AgentWalletLog;
import org.silver.shop.model.system.log.MerchantWalletLog;
import org.silver.util.StringEmptyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class WalletLogServiceImpl implements WalletLogService {

	@Autowired
	private WalletLogDao walletLogDao;



}
