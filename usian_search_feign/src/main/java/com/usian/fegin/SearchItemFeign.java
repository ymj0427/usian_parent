package com.usian.fegin;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("usian-search-service")
public interface SearchItemFeign {

    @RequestMapping("/service/searchItem/importAll")
    public boolean importAll();
}
