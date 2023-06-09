package com.lawencon.leaf.community.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lawencon.base.ConnHandler;
import com.lawencon.leaf.community.dao.CategoryDao;
import com.lawencon.leaf.community.model.Category;
import com.lawencon.leaf.community.pojo.PojoRes;
import com.lawencon.leaf.community.pojo.category.PojoCategoryReq;
import com.lawencon.leaf.community.pojo.category.PojoCategoryRes;
import com.lawencon.leaf.community.util.GenerateCodeUtil;

@Service
public class CategoryService extends BaseService<PojoCategoryRes> {
	
	@Autowired
	private CategoryDao categoryDao;
	private void valIdNull(PojoCategoryReq category) {
		if (category.getId() != null) {
			throw new RuntimeException("Id must be empty");
		}
	}

	private void valNonBk(PojoCategoryReq category) {
		if (category.getCategoryName() == null) {
			throw new RuntimeException("Content cannot be empty");
		}


	}

	private void valIdNotNull(PojoCategoryReq category) {
		if (category.getId() == null) {
			throw new RuntimeException("Id cannot be empty");
		}
	}

	private void valIdExist(String id) {
		if (categoryDao.getById(id).isEmpty()) {
			throw new RuntimeException("Id cannot be empty");
		}
	}
	@Override
	public PojoCategoryRes getById(String id) {
		final PojoCategoryRes pojoCategoryRes = new PojoCategoryRes();
		Category category = categoryDao.getById(id).get();
		pojoCategoryRes.setCategoryCode(category.getCategoryCode());
		pojoCategoryRes.setCategoryName(category.getCategoryName());
		pojoCategoryRes.setId(id);
		pojoCategoryRes.setVer(category.getVer());
		pojoCategoryRes.setIsActive(category.getIsActive());
		
		return pojoCategoryRes;
	}

	@Override
	public List<PojoCategoryRes> getAll() {
		final List<PojoCategoryRes> pojoCategoryRes = new ArrayList<>();
		List<Category> categories = categoryDao.getAll();
		for(int i=0;i<categories.size();i++) {
			PojoCategoryRes category = new PojoCategoryRes();
			category.setCategoryCode(categories.get(i).getCategoryCode());
			category.setCategoryName(categories.get(i).getCategoryName());
			category.setId(categories.get(i).getId());
			category.setVer(categories.get(i).getVer());
			category.setIsActive(categories.get(i).getIsActive());
			pojoCategoryRes.add(category);
		}
		return pojoCategoryRes;
	}
	
	public PojoRes insert(PojoCategoryReq data) {
		ConnHandler.begin();
		valIdNull(data);
		valNonBk(data);
		Category category = new Category();

		category.setCategoryName(data.getCategoryName());
		category.setCategoryCode(GenerateCodeUtil.generateCode(10));
		category.setIsActive(true);
		categoryDao.save(category);
		ConnHandler.commit();
		
		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Succes Create Category");
		return pojoRes;
	}
	
	public PojoRes update(PojoCategoryReq data) {
		ConnHandler.begin();
		valIdExist(data.getId());
		valIdNotNull(data);
		valNonBk(data);
		Category category = categoryDao.getById(data.getId()).get();
		category.setCategoryName(data.getCategoryName());
		category.setIsActive(true);
		categoryDao.save(category);
		ConnHandler.commit();
		
		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Category Updated");
		return pojoRes;
	}
	
	public PojoRes delete(String id) {
		
		try {
			ConnHandler.begin();
			valIdExist(id);
			categoryDao.deleteById(Category.class, id);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		final PojoRes pojoRes = new PojoRes();
		pojoRes.setMessage("Category Deleted");
		return pojoRes;
	}

}
