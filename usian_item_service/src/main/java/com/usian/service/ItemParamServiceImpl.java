package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.mapper.TbItemParamMapper;
import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamExample;
import com.usian.pojo.TbItemParamItem;
import com.usian.pojo.TbItemParamItemExample;
import com.usian.redis.RedisClient;
import com.usian.utils.PageResult;
import jdk.nashorn.internal.ir.ReturnNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ItemParamServiceImpl implements ItemParamService {

    @Autowired
    private TbItemParamMapper tbItemParamMapper;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;

    @Value("${ITEM_INFO}")
    private String ITEM_INFO;

    @Value("${PARAM}")
    private String PARAM;

    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;

    @Value("${SETNX_PARAM_LOCK_KEY}")
    private String SETNX_PARAM_LOCK_KEY;

    @Autowired
    private RedisClient redisClient;

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

    /**
     * 根据商品id查询商品规格
     * @param itemId
     * @return
     */
    @Override
    public TbItemParamItem selectTbItemParamItemByItemId(Long itemId) {
        //查询缓存
        TbItemParamItem tbItemParamItem = (TbItemParamItem) redisClient.get(
                ITEM_INFO + ":" + itemId + ":"+ PARAM);
        if(tbItemParamItem!=null){
            return tbItemParamItem;
        }
        //***********解决缓存击穿**************
        if (redisClient.setnx(SETNX_PARAM_LOCK_KEY+":"+itemId,itemId,30L)){
            //查询MySql
            TbItemParamItemExample example = new TbItemParamItemExample();
            TbItemParamItemExample.Criteria criteria = example.createCriteria();
            criteria.andItemIdEqualTo(itemId);
            List<TbItemParamItem> tbItemParamItemList = tbItemParamItemMapper.selectByExampleWithBLOBs(example);
            if(tbItemParamItemList!=null && tbItemParamItemList.size()>0){
                //把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":"+ PARAM,tbItemParamItemList.get(0));
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":"+ PARAM,ITEM_INFO_EXPIRE);
                return tbItemParamItemList.get(0);
            }else {
                //****************解决缓存穿透***********************
                //将空对象添加到redis中
                redisClient.set(ITEM_INFO + ":" + itemId + ":"+ PARAM,null);
                //设置缓存失效时间
                redisClient.expire(ITEM_INFO + ":" + itemId + ":"+ PARAM,30);
            }
            //删除锁
            redisClient.del(SETNX_PARAM_LOCK_KEY+":"+itemId);
            return tbItemParamItem;
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return selectTbItemParamItemByItemId(itemId);
    }
}
