package com.usian.service;

import com.usian.pojo.SearchItem;

import java.io.IOException;
import java.util.List;

public interface SearchItemService {

    public boolean importAll();

    List<SearchItem> selectByq(String q, Long page, Integer pageSize) throws IOException;

    int addDocement(String msg) throws IOException;
}
