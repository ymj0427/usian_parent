package com.usian.feign;

import com.usian.pojo.TbItem;
import com.usian.pojo.TbItemCat;
import com.usian.pojo.TbItemParam;
import com.usian.utils.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "usian-item-service")
public interface ItemServiceFeignClient {

    /**
     * 根据id查询商品
     * @param itemId
     * @return
     */
    @RequestMapping("/service/item/selectItemInfo")
    TbItem selectItemInfo(@RequestParam("itemId") Long itemId);

    /**
     * 查询所有商品，具有分页功能
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/service/item/selectTbItemAllByPage")
    PageResult selectTbItemAllByPage(@RequestParam Integer page, @RequestParam Integer rows);

    /**
     * 根据id查询商品类目
     * @param id
     * @return
     */
    @RequestMapping("/service/itemCat/selectItemCategoryByParentId")
    List<TbItemCat> selectItemCategoryByParentId(@RequestParam(defaultValue = "0") Long id);

    /**
     * 根据id查询商品规格
     * @param itemCatId
     * @return
     */
    @RequestMapping("/service/itemParam/selectItemParamByItemCatId/{itemCatId}")
    TbItemParam selectItemParamByItemCatId(@PathVariable Long itemCatId);

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/service/item/insertTbItem")
    Integer insertTbItem(TbItem tbItem,@RequestParam String desc,@RequestParam String itemParams);

    /**
     * 根据id删除商品
     * @param itemId
     * @return
     */
    @RequestMapping("/service/item/deleteItemById")
    Integer deleteItemById(@RequestParam Long itemId);

    /**
     * 规格参数查询
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/service/itemParam/selectItemParamAll")
    PageResult selectItemParamAll(@RequestParam Integer page,
                                  @RequestParam Integer rows);

    /**
     * 添加规格模板
     * @param itemCatId
     * @param paramData
     * @return
     */
    @RequestMapping("/service/itemParam/insertItemParam")
    Integer insertItemParam(@RequestParam Long itemCatId,@RequestParam String paramData);

    /**
     * 根据id删除规格模板
     * @param id
     * @return
     */
    @RequestMapping("/service/itemParam/deleteItemParamById")
    Integer deleteItemParamById(@RequestParam Long id);
}