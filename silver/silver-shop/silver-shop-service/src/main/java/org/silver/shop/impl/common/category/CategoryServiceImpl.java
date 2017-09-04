package org.silver.shop.impl.common.category;

import java.util.List;

import org.silver.shop.api.common.category.CategoryService;
import org.silver.shop.dao.common.category.CategoryDao;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;

@Service(interfaceClass=CategoryService.class)
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryDao categoryDao;

	@Override
	public List<Object> findAllfirstType() {
		return  categoryDao.findAllfirstType();
	}

	@Override
	public List<Object> findAllSecondType() {
		return categoryDao.findAllSecondType();
	}

	@Override
	public List<Object> findAllThirdType() {
		return categoryDao.findAllThirdType();
	}

}
