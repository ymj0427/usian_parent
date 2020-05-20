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
}
