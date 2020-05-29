package com.usian.controller;

import com.usian.feign.ItemServiceFeignClient;
import com.usian.utils.CatResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/frontend/itemCategory")
public class ItemCategoryController {

    @Autowired
    private ItemServiceFeignClient itemServiceFeignClient;

    /**
     * 首页左侧商品分类查询
     * @return
     */
    @RequestMapping("/selectItemCategoryAll")
    public Result selectItemCategoryAll(){
        CatResult catResult = itemServiceFeignClient.selectItemCategoryAll();
        if (catResult != null && catResult.getData().size() > 0){
            return Result.ok(catResult);
        }
        return Result.error("查无结果");
    }

}
