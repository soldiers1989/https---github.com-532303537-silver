package org.silver.shop.dao.common.base.impl;

import java.util.List;

import org.silver.shop.dao.BaseDaoImpl;
import org.silver.shop.dao.common.base.CountryDao;
import org.silver.shop.model.common.base.Country;
import org.springframework.stereotype.Repository;

@Repository("countryDao")
public class CountryDaoImpl extends BaseDaoImpl<Object> implements CountryDao {

}
