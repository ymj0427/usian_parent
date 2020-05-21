package com.usian.service;

import com.usian.pojo.TbContent;
import com.usian.utils.PageResult;

public interface ContentService {

    /**
     * 根据内容类目id查询所有内容
     * @param categoryId
     * @param page
     * @param rows
     * @return
     */
    PageResult selectTbContentAllByCategoryId(Long categoryId, Integer page, Integer rows);

    /**
     * 添加内容
     * @param tbContent
     * @return
     */
    Integer insertTbContent(TbContent tbContent);

    /**
     * 根据id删除内容
     * @param ids
     * @return
     */
    Integer deleteContentByIds(Long ids);
}
