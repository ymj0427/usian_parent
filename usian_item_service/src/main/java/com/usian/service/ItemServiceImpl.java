package com.usian.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.netflix.discovery.converters.Auto;
import com.usian.mapper.TbItemCatMapper;
import com.usian.mapper.TbItemDescMapper;
import com.usian.mapper.TbItemMapper;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.pojo.*;
import com.usian.redis.RedisClient;
import com.usian.utils.*;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private TbItemDescMapper tbItemDescMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;

    @Value("${portal_cateGory_redis_key}")
    private String portal_cateGory_redis_key;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${ITEM_INFO}")
    private String ITEM_INFO;

    @Value("${BASE}")
    private String BASE;

    @Value("${DESC}")
    private String DESC;

    @Value("${PARAM}")
    private String PARAM;

    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;

    @Value("${SETNX_DESC_LOCK_KEY}")
    private String SETNX_DESC_LOCK_KEY;

    @Value("${SETNX_BASC_LOCK_KEY}")
    private String SETNX_BASC_LOCK_KEY;

    /**
     * 商品查询
     * @param itemId
     * @return selectByPrimaryKey
     */
    @Override
    public TbItem selectItemInfo(Long itemId){
        TbItem tbItem = (TbItem)redisClient.get(ITEM_INFO + ":" + itemId + ":"+ BASE);
        if (tbItem != null) {
            return tbItem;
        }

        //*************解决缓存击穿*************
        if (redisClient.setnx(SETNX_DESC_LOCK_KEY+":"+itemId,tbItem,30L)){
            tbItem = tbItemMapper.selectByPrimaryKey(itemId);
            if (tbItem != null){
                //将数据存入reids缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":"+ BASE,tbItem);
                //设置redis缓存失效时间
                redisClient.expire(ITEM_INFO + ":" + itemId + ":"+ BASE,ITEM_INFO_EXPIRE);
                return tbItem;
            }else {
                //*********************解决缓存穿透*************************************
                //将空数据缓存到redis中
                redisClient.set(ITEM_INFO + ":" + itemId + ":"+ BASE,null);
                //设置缓存失效时间
                redisClient.expire(ITEM_INFO + ":" + itemId + ":"+ BASE,30);
            }
            //删除锁
            redisClient.del(SETNX_BASC_LOCK_KEY+":"+itemId);
            return tbItem;
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return selectItemInfo(itemId);
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
        example.setOrderByClause("updated DESC");
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

    /**
     * 商品添加
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer insertTbItem(TbItem tbItem, String desc, String itemParams) {
        //补齐TbItem数据
        Long itemId = IDUtils.genItemId();
        Date date = new Date();  //创建日期对象
        tbItem.setId(itemId);
        tbItem.setStatus((byte) 1);
        tbItem.setUpdated(date);
        tbItem.setCreated(date);
        Integer tbItemNum = tbItemMapper.insertSelective(tbItem);

        //补齐商品描述对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(itemId);
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setCreated(date);
        tbItemDesc.setUpdated(date);
        Integer tbitemDescNum = tbItemDescMapper.insertSelective(tbItemDesc);

        //补齐商品规格参数
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(itemId);
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setCreated(date);
        tbItemParamItem.setUpdated(date);
        Integer tbitemParamNum = tbItemParamItemMapper.insertSelective(tbItemParamItem);

        //添加商品发布消息到MQ
        amqpTemplate.convertAndSend("item_exchage","item.add",itemId);

        System.out.println(tbItemNum + tbitemDescNum + tbitemParamNum+"============================");
        return tbItemNum + tbitemDescNum + tbitemParamNum;
    }

    /**
     * 根据id删除商品
     * @param itemId
     * @return
     */
    @Override
    public Integer deleteItemById(Long itemId) {
        Integer i = tbItemMapper.deleteByPrimaryKey(itemId);
        //删除redis缓存
        redisClient.del(ITEM_INFO+":"+itemId+":"+BASE);
        redisClient.del(ITEM_INFO+":"+itemId+":"+DESC);
        redisClient.del(ITEM_INFO+":"+itemId+":"+PARAM);
        return i;
    }

    /**
     * 预更新
     * @param itemId
     * @return
     */
    @Override
    public Map<String, Object> preUpdateItem(Long itemId) {
        Map<String, Object> map = new HashMap<>();

        //查询item信息
        TbItem tbItems = tbItemMapper.selectByPrimaryKey(itemId);
        map.put("item",tbItems);

        //查询描述
        TbItemDesc tbItemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
        map.put("itemDesc",tbItemDesc.getItemDesc());
        //查询类目
        TbItemCat tbItemCat = tbItemCatMapper.selectByPrimaryKey(tbItems.getCid());
        map.put("itemCat",tbItemCat.getName());
        //查询规格模板
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        List<TbItemParamItem> list = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
        if (list != null && list.size() > 0){
            map.put("itemParamItem",list.get(0).getParamData());
        }


        return map;
    }

    /**
     * 首页左侧商品分类查询
     * @return
     */
    @Override
    public CatResult selectItemCategoryAll() {
        //查询缓存
        CatResult catResultRedis = (CatResult) redisClient.get(portal_cateGory_redis_key);
        if (catResultRedis != null){
            System.out.println("======从Redis获取======");
            return catResultRedis;
        }

        CatResult catResult = new CatResult();
        //查询商品分类
        catResult.setData(getCatList(0L));

        //添加到缓存
        redisClient.set(portal_cateGory_redis_key,catResult);
        System.out.println("======从后台查询=======");
        return catResult;
    }

    /**
     * 私有方法，查询商品分类
     */
    private List<?> getCatList(Long parentId) {
        //创建查询条件
        TbItemCatExample tbItemCatExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = tbItemCatExample.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<TbItemCat> tbItemCatList = tbItemCatMapper.selectByExample(tbItemCatExample);

        List resultlist = new ArrayList<>();

        int count = 0;

        for (TbItemCat tbItemCat : tbItemCatList){
            if (tbItemCat.getIsParent()){
                CatNode catNode = new CatNode();
                catNode.setName(tbItemCat.getName());
                catNode.setItem(getCatList(tbItemCat.getId()));
                resultlist.add(catNode);
                count ++;
                //只取18条数据
                if (count == 18){
                    break;
                }
            }else {
                resultlist.add(tbItemCat.getName());
            }
        }
        return resultlist;
    }


    /**
     * 商品修改
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer updateTbItem(TbItem tbItem, String desc, String itemParams) {
        //补齐TbItem数据
        //创建日期对象
        Date date = new Date();
        tbItem.setStatus((byte) 1);
        tbItem.setUpdated(date);
        Integer tbItemNum = tbItemMapper.updateByPrimaryKeySelective(tbItem);

        //补齐商品描述对象
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(tbItem.getId());
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setUpdated(date);
        Integer tbitemDescNum = tbItemDescMapper.updateByPrimaryKeySelective(tbItemDesc);

        //补齐商品规格参数
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(tbItem.getId());
        List<TbItemParamItem> list = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
        TbItemParamItem tbItemParamItem = list.get(0);
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setUpdated(date);
        Integer tbitemParamNum = tbItemParamItemMapper.updateByPrimaryKeySelective(tbItemParamItem);

        Long id = tbItem.getId();
        //删除redis缓存
        redisClient.del(ITEM_INFO+":"+id+":"+BASE);
        redisClient.del(ITEM_INFO+":"+id+":"+DESC);
        redisClient.del(ITEM_INFO+":"+id+":"+PARAM);
        return tbItemNum + tbitemDescNum + tbitemParamNum;
    }

    @Override
    public TbItemDesc selectItemDescByItemId(Long itemId) {
        //查询缓存
        TbItemDesc tbItemDesc = (TbItemDesc) redisClient.get(
                ITEM_INFO + ":" + itemId + ":"+ DESC);
        if(tbItemDesc!=null){
            return tbItemDesc;
        }
        //***********解决缓存击穿*******************
        if (redisClient.setnx(SETNX_DESC_LOCK_KEY+":"+itemId,itemId,30)){
            //MySql中查询
            TbItemDescExample example = new TbItemDescExample();
            TbItemDescExample.Criteria criteria = example.createCriteria();
            criteria.andItemIdEqualTo(itemId);
            List<TbItemDesc> itemDescList =
                    this.tbItemDescMapper.selectByExampleWithBLOBs(example);
            if(itemDescList!=null && itemDescList.size()>0){
                //把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":"+ DESC,itemDescList.get(0));
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":"+ DESC,ITEM_INFO_EXPIRE);
                return itemDescList.get(0);
            }else{
                //***************解决缓存穿透*****************************8
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC,null);
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,30L);
            }
            //删除锁
            redisClient.del(SETNX_DESC_LOCK_KEY+":"+itemId);
            return tbItemDesc;
        }else{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return selectItemDescByItemId(itemId);
    }
}
