package com.usian.service;

import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;

public interface ItemService {

    //根据id查询商品
    TbItem selectItemInfo(Long itemId);

    //查询所有商品，具有分页功能
    PageResult selectTbItemAllByPage(Integer page, Integer rows);

    //添加商品
    Integer insertTbItem(TbItem tbItem, String desc, String itemParams);

    //删除商品
    Integer deleteItemById(Long itemId);
}
