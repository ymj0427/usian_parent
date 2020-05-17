package com.usian.service;

import com.usian.pojo.TbItemCat;

import java.util.List;

public interface ItemCatService {
    /**
     * 根据id查询商品类目
     * @param id
     * @return
     */
    List<TbItemCat> selectItemCategoryByParentId(Long id);
}
