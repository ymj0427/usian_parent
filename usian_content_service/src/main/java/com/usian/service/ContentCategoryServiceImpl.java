package com.usian.service;

import com.usian.mapper.TbContentCategoryMapper;
import com.usian.pojo.TbContentCategory;
import com.usian.pojo.TbContentCategoryExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private TbContentCategoryMapper tbContentCategoryMapper;

    /**
     * 根据当前节点id查询子节点
     * @param id
     * @return
     */
    @Override
    public List<TbContentCategory> selectContentCategoryByParentId(Long id) {

        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(id);
        List<TbContentCategory> list = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);
        return list;
    }

    /**
     * 添加内容分类
     * @param tbContentCategory
     * @return
     */
    @Override
    public Integer insertContentCategory(TbContentCategory tbContentCategory) {
        //添加
        Date date = new Date();
        tbContentCategory.setSortOrder(1);
        tbContentCategory.setStatus(1);
        tbContentCategory.setIsParent(false);
        tbContentCategory.setUpdated(date);
        tbContentCategory.setCreated(date);
        Integer num = tbContentCategoryMapper.insertSelective(tbContentCategory);

        //查询其父节点
        TbContentCategory ContentCategory = tbContentCategoryMapper.selectByPrimaryKey(tbContentCategory.getParentId());

        //判断其父节点是不是父节点，如果不是，则修改为父节点
        if (!ContentCategory.getIsParent()){
            ContentCategory.setIsParent(true);
            ContentCategory.setUpdated(date);
            tbContentCategoryMapper.updateByPrimaryKeySelective(ContentCategory);
        }

        return num;
    }

    /**
     * 根据categoryId删除内容分类
     * @param categoryId
     * @return
     */
    @Override
    public Integer deleteContentCategoryById(Long categoryId) {
        //查询当前节点
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(categoryId);
        //判断是否是父节点
        if (tbContentCategory.getIsParent() == true){ //是父节点，不删除
            return 0;
        }
        //否则 删除
        Integer num = tbContentCategoryMapper.deleteByPrimaryKey(categoryId);

        //查询当前节点下的兄弟节点
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(tbContentCategory.getParentId());
        List<TbContentCategory> categoryList = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);

        //判断 是否是父节点，不是，把父id改为false
        if (categoryList.size() == 0){
            TbContentCategory contentCategory = new TbContentCategory();
            contentCategory.setIsParent(false);
            contentCategory.setUpdated(new Date());
            contentCategory.setParentId(tbContentCategory.getParentId());
            tbContentCategoryMapper.updateByPrimaryKeySelective(contentCategory);
        }

        return 200;
    }

    /**
     * 根据id修改分类内容
     * @param tbContentCategory
     * @return
     */
    @Override
    public Integer updateContentCategory(TbContentCategory tbContentCategory) {
        Date date = new Date();
        tbContentCategory.setUpdated(date);
        tbContentCategory.setCreated(date);

        return tbContentCategoryMapper.updateByPrimaryKeySelective(tbContentCategory);
    }
}
