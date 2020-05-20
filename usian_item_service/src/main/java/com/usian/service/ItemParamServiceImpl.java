package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemParamMapper;
import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamExample;
import com.usian.utils.PageResult;
import jdk.nashorn.internal.ir.ReturnNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ItemParamServiceImpl implements ItemParamService {

    @Autowired
    private TbItemParamMapper tbItemParamMapper;

    /**
     * 根据id查询商品规格
     * @param itemCatId
     * @return
     */
    @Override
    public TbItemParam selectItemParamByItemCatId(Long itemCatId) {
        TbItemParamExample example = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = example.createCriteria();
        criteria.andItemCatIdEqualTo(itemCatId);
        List<TbItemParam> itemParamList = tbItemParamMapper.selectByExampleWithBLOBs(example);
        if (itemParamList != null && itemParamList.size()>0){
            return itemParamList.get(0);
        }
        return null;
    }


    /**
     * 规格参数查询
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectItemParamAll(Integer page, Integer rows) {

        PageHelper.startPage(page,rows);

        TbItemParamExample example = new TbItemParamExample();
        example.setOrderByClause("updated DESC");
        List<TbItemParam> itemParamList = tbItemParamMapper.selectByExampleWithBLOBs(example);

        PageInfo<TbItemParam> pageInfo = new PageInfo<>(itemParamList);

        PageResult pageResult = new PageResult();

        pageResult.setPageIndex(page);
        pageResult.setResult(pageInfo.getList());
        pageResult.setTotalPage(pageInfo.getTotal());

        return pageResult;
    }

    /**
     * 添加规格模板
     * @param itemCatId
     * @param paramData
     * @return
     */
    @Override
    public Integer insertItemParam(Long itemCatId, String paramData) {
        //判断itemCatId是否已存在
        TbItemParamExample example = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = example.createCriteria();
        criteria.andItemCatIdEqualTo(itemCatId);
        List<TbItemParam> list = tbItemParamMapper.selectByExampleWithBLOBs(example);
        if (list.size() > 0){  //存在，不添加
            return 0;
        }

        //否则，规格模板不存，能添加
        Date date = new Date();
        TbItemParam itemParam = new TbItemParam();
        itemParam.setItemCatId(itemCatId);
        itemParam.setParamData(paramData);
        itemParam.setCreated(date);
        itemParam.setUpdated(date);

        return tbItemParamMapper.insertSelective(itemParam);
    }

    /**
     * 根据id删除规格模板
     * @param id
     * @return
     */
    @Override
    public Integer deleteItemParamById(Long id) {
        return tbItemParamMapper.deleteByPrimaryKey(id);
    }
}
