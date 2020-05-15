package com.usian.feign;

import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}