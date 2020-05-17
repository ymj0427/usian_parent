package com.usian.service;

import com.usian.pojo.TbItemParam;

public interface ItemParamService {

    /**
     * 根据id查询商品规格
     * @param itemCatId
     * @return
     */
    TbItemParam selectItemParamByItemCatId(Long itemCatId);
}
