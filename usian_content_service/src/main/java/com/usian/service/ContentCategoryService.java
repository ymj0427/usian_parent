package com.usian.service;

import com.usian.pojo.TbContentCategory;

import java.util.List;

public interface ContentCategoryService {

    /**
     * 根据当前节点id查询子节点
     * @param id
     * @return
     */
    List<TbContentCategory> selectContentCategoryByParentId(Long id);

    /**
     * 添加内容分类
     * @param tbContentCategory
     * @return
     */
    Integer insertContentCategory(TbContentCategory tbContentCategory);

    /**
     * 根据categoryId删除内容分类
     * @param categoryId
     * @return
     */
    Integer deleteContentCategoryById(Long categoryId);

    /**
     * 根据id修改分类内容
     * @param tbContentCategory
     * @return
     */
    Integer updateContentCategory(TbContentCategory tbContentCategory);
}
