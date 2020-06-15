package com.usian.controller;

import com.usian.feign.ItemServiceFeignClient;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbItemDesc;
import com.usian.pojo.TbItemParamItem;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/frontend/detail")
public class DetailController {

    @Autowired
    private ItemServiceFeignClient itemServiceFeignClient;

    /**
     * 查询商品基本信息
     */
    @RequestMapping("/selectItemInfo")
    public Result selectItemInfo(Long itemId) {
        TbItem tbItem = itemServiceFeignClient.selectItemInfo(itemId);
        if (tbItem != null) {
            return Result.ok(tbItem);
        }
        return Result.error("查无结果");
    }

    /**
     * 查询商品介绍
     */
    @RequestMapping("/selectItemDescByItemId")
    public Result selectItemDescByItemId(Long itemId){
        TbItemParamItem tbItemDesc = itemServiceFeignClient.selectItemDescByItemId(itemId);
        if(tbItemDesc != null){
            return Result. ok (tbItemDesc);
        }
        return Result.error ("查无结果");
    }

    /**
     * 根据商品 ID 查询商品规格参数
     */
    @RequestMapping("/selectTbItemParamItemByItemId")
    public Result selectTbItemParamItemByItemId(Long itemId){
        TbItemParamItem tbItemParamItem =
                itemServiceFeignClient.selectTbItemParamItemByItemId(itemId);
        if(tbItemParamItem != null){
            return Result.ok(tbItemParamItem);
        }
        return Result. error ("查无结果");
    }
}
