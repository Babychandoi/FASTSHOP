package com.fastshop.net.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fastshop.net.model.Category;
import com.fastshop.net.model.CategoryDetail;
import com.fastshop.net.model.Product;
import com.fastshop.net.model.ProductDetail;
import com.fastshop.net.model.ProductDetailBackup;
import com.fastshop.net.repository.CategoryDAO;
import com.fastshop.net.repository.CategoryDetailDAO;
import com.fastshop.net.repository.ProductDetailDAO;
import com.fastshop.net.service.CategoryDetailService;
import com.fastshop.net.service.ProductDetailService;
import com.fastshop.net.service.ProductService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductDetailServiceImpl implements ProductDetailService {
    @Autowired
    ProductDetailDAO productDetailDAO;
    @Autowired
    CategoryDAO categoryDAO;
    @Autowired
    CategoryDetailDAO categoryDetailDAO;
    @Autowired
    ProductService productService;
    @Autowired
    CategoryDetailService categoryDetailService;

    @Override
    @Transactional
    public List<ProductDetail> findAll() {
        return productDetailDAO.findAll();
    }

    @Override
    @Transactional
    public List<ProductDetail> findByProductId(Integer productId) {
        return productDetailDAO.findByProductId(productId);
    }

    @Override
    @Transactional
    public List<ProductDetailBackup> getByProductId(Integer productId) {
        List<ProductDetailBackup> list = new ArrayList<>();
        List<ProductDetail> productDetails = productDetailDAO.findByProductId(productId);
        if (productDetails.size() > 0) {
            for (ProductDetail productDetail : productDetails) {
                ProductDetailBackup pdb = new ProductDetailBackup();
                pdb.setId(productDetail.getId());
                pdb.setProductId(productId);
                pdb.setCategoryDetailId(productDetail.getCategoryDetailId());
                pdb.setInfo(productDetail.getInfo());
                pdb.setProperty(categoryDetailDAO.findById(productDetail.getCategoryDetailId()).get().getProperty());
                list.add(pdb);
            }
        }
        else {
            Product product = productService.findById(productId);
            Category category = product.getCategory();
            List<CategoryDetail> l = categoryDetailService.findByCategory(category);
            for (CategoryDetail cd : l) {
                ProductDetailBackup pdb = new ProductDetailBackup();
                pdb.setProductId(productId);
                pdb.setCategoryDetailId(cd.getId());
                pdb.setInfo("");
                pdb.setProperty(cd.getProperty());
                list.add(pdb);
            }
        }
        return list;
    }

    @Override
    @Transactional
    public Map<String, String> getAllOfCategoryDetailAndInfo(Integer productId) {
        try {
            List<ProductDetailBackup> list = getByProductId(productId);
            Map<String, String> map = new HashMap<>();
            for (ProductDetailBackup productDetailBackup : list) {
                map.put(productDetailBackup.getProperty(), productDetailBackup.getInfo());

            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public ProductDetail save(ProductDetail productDetail) {
        productDetailDAO.save(productDetail);
        return productDetail;
    }

    @Override
    @Transactional
    public ProductDetail delete(ProductDetail productDetail) {
        productDetailDAO.delete(productDetail);
        return productDetail;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        productDetailDAO.deleteById(id);
    }

    @Override
    @Transactional
    public ProductDetail findById(Long id) {
        return productDetailDAO.findById(id).get();
    }

    @Override
    @Transactional
    public Boolean existAnyProductDetailByProductId(Integer productId) {
        List<ProductDetailBackup> list = getByProductId(productId);
        for (ProductDetailBackup productDetailBackup : list) {
            if (!productDetailBackup.getInfo().equals("")) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public ProductDetail findByCategoryDetailIdAndProductId(String category_detail_id, Integer productId) {
        return productDetailDAO.findByCategoryDetailIdAndProductId(category_detail_id, productId).get();
    }
}
