package com.ibook.mall.service.impl;

import com.ibook.mall.consts.MallConst;
import com.ibook.mall.dao.CategoryMapper;
import com.ibook.mall.pojo.Category;
import com.ibook.mall.service.ICategoryService;
import com.ibook.mall.vo.CategoryVo;
import com.ibook.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements ICategoryService {
    //如何实现将多个类目进行嵌套并排序:
    //不要循环查询(查到所有父类目,然后循环父类目,找子类目)
    //可以一次性将所有类目全都查出来,然后在拿到的数据里面操作
    //使用递归

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 耗时:http(请求) > 磁盘(mysql) > (内存)Java程序
     * mysql(内网+磁盘)
     * 所以最忌讳的就是在for循环中写http的网络请求或者是sql,太耗时了!
     * @return
     */
    @Override
    public ResponseVo<List<CategoryVo>> selectAll() {
        List<Category> categories = categoryMapper.selectAll();
//        List<CategoryVo> categoryVoList = new ArrayList<>();
        //查出parent_id=0,使用for循环或者lambda表达式
//        for (Category category : categories) {
//            if (category.getParentId().equals(MallConst.ROOT_PARENT_ID)) {
//                //说明是一级目录
//                CategoryVo categoryVo = new CategoryVo();
//                //Spring提供的对象属性拷贝
//                BeanUtils.copyProperties(category, categoryVo);
//                categoryVoList.add(categoryVo);
//            }
//        }
        //lambda + stream表达式,->属于lambda表达式,.stream.filter等属于stream表达式
        //相比于for循环这个方法不用定义一个新的List
        //不要为了用lambda而用lambda,阅读性比较差
        //在lambda表达式中顺便将根目录排序
        List<CategoryVo> categoryVoList = categories.stream()
                .filter(e -> e.getParentId().equals(MallConst.ROOT_PARENT_ID))
                .map(this::category2CategoryVo)
                .sorted(Comparator.comparing(CategoryVo::getSortOrder).reversed())
                .collect(Collectors.toList());

        //查询子目录并排序
        findSubCategory(categoryVoList, categories);

        return ResponseVo.success(categoryVoList);

    }

    //为了不让递归函数每次递归都查一次数据库,只能先将所有结果都找到然后将这个集合传入递归方法中
    @Override
    public void findSubCategoryId(Integer id, Set<Integer> resultSet) {
        List<Category> categories = categoryMapper.selectAll();
        findSubCategoryId(id, resultSet, categories);
    }

    //递归函数功能:
    //  传入父类目的id与装着结果集的resultSet,与装着所有类目的List
    //  将父类目所有的子类目子子类目都放到resultSet中去
    private void findSubCategoryId(Integer id, Set<Integer> resultSet, List<Category> categories) {
        for (Category category : categories) {
            //如果当前类别的父目录为传入目录的id,就将其放到resultSet中
            if (category.getParentId().equals(id)) {
                resultSet.add(category.getId());
                findSubCategoryId(category.getId(), resultSet, categories);
            }
        }
    }

    //第一个参数对应了返回的结果(传入的是一级类目,返回的是将一级类目的子类目都连接在上面后的List),
    // 第二个参数对应了数据源(从数据源中拿到类目对象)
    //递归函数功能:
    //  输入所有的一级类目与所有类目,将所有类目中的子类目与父类目相连,并按照sort_order字段进行排序
    private void findSubCategory(List<CategoryVo> categoryVoList, List<Category> categories) {
        //base case
        if (categoryVoList.isEmpty()) return;
        //遍历拿到一级目录list中的数据然后再去查
        for (CategoryVo categoryVo : categoryVoList) {
            List<CategoryVo> subCategoryVoList = new ArrayList<>();
            //第二层循环是从所有级别目录中遍历去查
            //拿到每一个一级目录,然后去遍历所有的其他级别目录
            for (Category category : categories) {
                //如果等于,说明当前遍历到内层循环中的目录是最外层循环遍历到的目录的子目录
                //此时需要设置subCategory,并继续往下查,一直查到的是null为止
                if (categoryVo.getId().equals(category.getParentId())) {
                    CategoryVo subCategoryVo = category2CategoryVo(category);
                    subCategoryVoList.add(subCategoryVo);
                }
                //在将其加入当前父类目的子类目列表之前,对这个子类目组成的列表进行排序再放入
                subCategoryVoList.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());

                categoryVo.setSubCategories(subCategoryVoList);
                findSubCategory(subCategoryVoList, categories);
            }
        }
    }

    private CategoryVo category2CategoryVo(Category category) {
        CategoryVo categoryVo = new CategoryVo();
        BeanUtils.copyProperties(category, categoryVo);
        return categoryVo;
    }
}
