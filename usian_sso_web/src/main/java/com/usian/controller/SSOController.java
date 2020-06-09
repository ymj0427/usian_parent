package com.usian.controller;

import com.usian.feign.SSOServiceFrign;
import com.usian.pojo.TbUser;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/frontend/sso")
public class SSOController {

    @Autowired
    private SSOServiceFrign ssoServiceFrign;

    @RequestMapping("/checkUserInfo/{checkValue}/{checkFlag}")
    public Result checkUserInfo(@PathVariable String checkValue,@PathVariable Integer checkFlag){
        Boolean checkUserInfo = ssoServiceFrign.checkUserInfo(checkValue,checkFlag);
        if (checkUserInfo){
            return Result.ok();
        }
        return Result.error("校验失败");
    }

    @RequestMapping("/userRegister")
    public Result userRegister(TbUser tbUser){
        Integer userRegister = ssoServiceFrign.userRegister(tbUser);
        if (userRegister==1){
            return Result.ok();
        }
        return Result.error("注册失败");
    }

    @RequestMapping("/userLogin")
    public Result userLogin(@RequestParam String username,@RequestParam String password){
        Map map = ssoServiceFrign.userLogin(username,password);
        if (map != null){
            return Result.ok(map);
        }
        return Result.error("登陆失败");
    }

    @RequestMapping("/getUserByToken/{token}")
    public Result getUserByToken(@PathVariable String token){
        TbUser tbUser = ssoServiceFrign.getUserByToken(token);
        if(tbUser!=null){
            return Result.ok();
        }
        return Result.error("登录过期");
    }

    @RequestMapping("/logOut")
    public Result logOut(@RequestParam String token){
        Boolean logOut = ssoServiceFrign.logOut(token);
        if (logOut){
            return Result.ok();
        }
        return Result.error("退出失败");
    }
}
