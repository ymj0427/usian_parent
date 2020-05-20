package com.usian.controller;

import com.usian.feign.ItemServiceFeignClient;
import com.usian.pojo.TbItemParam;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/itemParam")
public class ItemParamController {

    @Autowired
    public ItemServiceFeignClient itemServiceFeignClient;

    /**
     * 根据id查询商品规格
     * @param itemCatId
     * @return
     */
    @RequestMapping("/selectItemParamByItemCatId/{itemCatId}")
    public Result selectItemParamByItemCatId(@PathVariable Long itemCatId){
        TbItemParam tbItemParam = itemServiceFeignClient.selectItemParamByItemCatId(itemCatId);
        if (tbItemParam != null){
            return Result.ok(tbItemParam);
        }
        return Result.error("查无结果");
    }

    /**
     * 规格参数查询
     * @return
     */
    @RequestMapping("/selectItemParamAll")
    public Result selectItemParamAll(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "100") Integer rows){
        PageResult pageResult = itemServiceFeignClient.selectItemParamAll(page,rows);
        if (pageResult.getResult() != null && pageResult.getResult().size() > 0){
            return Result.ok(pageResult);
        }
        return Result.error("查无结果");
    }

    /**
     * 添加规格模板
     * @param itemCatId
     * @param paramData
     * @return
     */
    @RequestMapping("/insertItemParam")
    public Result insertItemParam(@RequestParam Long itemCatId,@RequestParam String paramData){
        Integer insParamNum = itemServiceFeignClient.insertItemParam(itemCatId,paramData);
        if (insParamNum == 1){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    /**
     * 根据id删除规格模板
     * @param id
     * @return
     */
    @RequestMapping("/deleteItemParamById")
    public Result deleteItemParamById(@RequestParam Long id){
        Integer delParamNum = itemServiceFeignClient.deleteItemParamById(id);
        if (delParamNum == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }
}
