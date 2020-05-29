package com.usian.controller;

import com.usian.feign.ItemServiceFeignClient;
import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/backend/item")
public class ItemController {

    @Autowired
    private ItemServiceFeignClient itemServiceFeignClient;

    /**
     * 查询商品基本信息
     */

    @RequestMapping("/selectItemInfo")
    public Result selectItemInfo(Long itemId){
        TbItem tbItem = itemServiceFeignClient.selectItemInfo(itemId);
        if (tbItem != null){
            return Result.ok(tbItem);
        }
        return Result.error("查无结果");
    }

    /**
     * 查询所有商品，具有分页功能
     * @param page
     * @param rows
     * @return Result
     */
    @RequestMapping("/selectTbItemAllByPage")
    public Result selectTbItemAllByPage(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "3") Integer rows){
        PageResult pageResult = itemServiceFeignClient.selectTbItemAllByPage(page, rows);
        if (pageResult != null && pageResult.getResult() != null
                               && pageResult.getResult().size()>0){
            return Result.ok(pageResult);
        }
        return Result.error("查无结果");
    }

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/insertTbItem")
    public Result insertTbItem(TbItem tbItem,String desc,String itemParams){
        Integer insertItemNum = itemServiceFeignClient.insertTbItem(tbItem,desc,itemParams);
        if (insertItemNum==3){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    /**
     * 根据id删除商品
     * @param itemId
     * @return
     */
    @RequestMapping("/deleteItemById")
    public Result deleteItemById(Long itemId){
       Integer tbitemId =  itemServiceFeignClient.deleteItemById(itemId);
       if (tbitemId == 1){
           return Result.ok();
       }
       return Result.error("删除失败");
    }

    /**
     * 预更新
     * @param itemId
     * @return
     */
    @RequestMapping("/preUpdateItem")
    public Result preUpdateItem(Long itemId){
        Map<String,Object> preItemResult = itemServiceFeignClient.preUpdateItem(itemId);
        if (preItemResult != null){
            return Result.ok(preItemResult);
        }
        return Result.error("预更新失败");
    }

    /**
     * 修改商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/updateTbItem")
    public Result updateTbItem(TbItem tbItem,String desc,String itemParams){
        Integer insertItemNum = itemServiceFeignClient.updateTbItem(tbItem,desc,itemParams);
        if (insertItemNum==3){
            return Result.ok();
        }
        return Result.error("修改失败");
    }
}
