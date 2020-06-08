package com.usian.service;

import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamItem;
import com.usian.utils.PageResult;

public interface ItemParamService {

    /**
     * 根据id查询商品规格
     * @param itemCatId
     * @return
     */
    TbItemParam selectItemParamByItemCatId(Long itemCatId);

    /**
     * 规格参数查询
     * @param page
     * @param rows
     * @return
     */
    PageResult selectItemParamAll(Integer page, Integer rows);

    /**
     * 添加规格模板
     * @param itemCatId
     * @param paramData
     * @return
     */
    Integer insertItemParam(Long itemCatId, String paramData);

    /**
     * 根据id删除规格模板
     * @param id
     * @return
     */
    Integer deleteItemParamById(Long id);

    /**
     * 根据商品 ID 查询商品规格
     */
    TbItemParamItem selectTbItemParamItemByItemId(Long itemId);
}
