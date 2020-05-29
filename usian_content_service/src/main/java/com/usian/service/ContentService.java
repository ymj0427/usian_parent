package com.usian.service;

import com.usian.pojo.TbContent;
import com.usian.utils.AdNode;
import com.usian.utils.PageResult;

import java.util.List;

public interface ContentService {

    // 根据内容类目id查询所有内容
    PageResult selectTbContentAllByCategoryId(Long categoryId, Integer page, Integer rows);

    // 添加内容
    Integer insertTbContent(TbContent tbContent);

    // 根据id删除内容
    Integer deleteContentByIds(Long ids);

    //大广告查询
    List<AdNode> selectFrontendContentByAD();
}
