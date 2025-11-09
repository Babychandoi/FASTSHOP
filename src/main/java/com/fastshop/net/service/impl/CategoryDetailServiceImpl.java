package com.fastshop.net.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fastshop.net.model.Category;
import com.fastshop.net.model.CategoryDetail;
import com.fastshop.net.repository.CategoryDetailDAO;
import com.fastshop.net.service.CategoryDetailService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryDetailServiceImpl implements CategoryDetailService{
    @Autowired
    CategoryDetailDAO categoryDetailDAO;

    @Override
    @Transactional
    public void save(CategoryDetail categoryDetail) {
        categoryDetailDAO.save(categoryDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDetail> findByCategory(Category category) {
        return categoryDetailDAO.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPropertyByCategory(Category category) {
        return categoryDetailDAO.getPropertyByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDetail findById(String id) {
        return categoryDetailDAO.findById(id).get();
    }
}
