package com.ibook.mall.controller;

import com.ibook.mall.service.impl.CategoryServiceImpl;
import com.ibook.mall.vo.CategoryVo;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryService;

    @GetMapping("/categories")
    public ResponseVo<List<CategoryVo>> selectAll() {

        return categoryService.selectAll();

    }
}
