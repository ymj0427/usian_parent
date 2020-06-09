package com.usian.feign;

import com.usian.pojo.TbUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("usian-sso-service")
public interface SSOServiceFrign {

    @RequestMapping("/service/sso/checkUserInfo/{checkValue}/{checkFlag}")
    Boolean checkUserInfo(@PathVariable String checkValue,@PathVariable Integer checkFlag);

    @RequestMapping("/service/sso/userRegister")
    public Integer userRegister(TbUser tbUser);

    @RequestMapping("/service/sso/userLogin")
    public Map userLogin(@RequestParam String username, @RequestParam String password);

    @RequestMapping("/service/sso/getUserByToken/{token}")
    public TbUser getUserByToken(@PathVariable String token);

    @RequestMapping("/service/sso/logOut")
    public Boolean logOut(@RequestParam String token);
}
