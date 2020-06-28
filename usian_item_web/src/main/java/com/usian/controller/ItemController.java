package com.usian.controller;

import com.usian.feign.ItemServiceFeignClient;
import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @RequestMapping(value = "/selectItemInfo",method = RequestMethod.POST)
    @ApiOperation(value = "查询商品基本信息",notes = "根据itemId查询该商品的基本信息")
    @ApiImplicitParam(name = "itemId",type = "Long",value = "商品id")
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
    @RequestMapping(value = "/selectTbItemAllByPage",method = RequestMethod.GET)
    @ApiOperation(value = "查询商品并分页处理",notes = "分页查询商品信息，每页显示2条")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",
                    type = "Integer",value = "页码",defaultValue = "1"),
            @ApiImplicitParam(name="rows",
                    type = "Integer",value = "每页多少条",defaultValue = "2")
    })
    public Result selectTbItemAllByPage(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "2") Integer rows){
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
    @RequestMapping(value = "/insertTbItem",method = RequestMethod.POST)
    @ApiOperation(value = "添加商品",notes = "添加商品及描述和规格参数信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="desc",type = "String",value = "商品描述信息"),
            @ApiImplicitParam(name="itemParams",type = "String",value = "商品规格参数")
    })
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
