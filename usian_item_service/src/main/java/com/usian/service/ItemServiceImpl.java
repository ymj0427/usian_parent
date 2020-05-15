package com.usian.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemMapper;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbItemExample;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.util.List;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper tbItemMapper;

    /**
     * 商品查询
     * @param itemId
     * @return selectByPrimaryKey
     */
    @Override
    public TbItem selectItemInfo(Long itemId){
        return tbItemMapper.selectByPrimaryKey(itemId);
    }

    /**
     * 查询所有的商品，增添分页功能
     * @param page
     * @param rows
     * @return result
     */
    @Override
    public PageResult selectTbItemAllByPage(Integer page,Integer rows){
        PageHelper.startPage(page,rows);
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo((byte) 1);
        List<TbItem> list = tbItemMapper.selectByExample(example);
        PageInfo<TbItem> pageInfo = new PageInfo<TbItem>(list);
        PageResult result = new PageResult();
        result.setPageIndex(page);
        result.setTotalPage(pageInfo.getTotal());
        result.setResult(list);
        return result;
    }

}
