package com.sshweb.web;

import com.sshweb.common.Result;
import com.sshweb.common.SSHClientUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Created by Administrator on 2019/1/22.
 */
@Controller
public class SSHWeb {

    @RequestMapping("/login")
    public String index(){
        return "index";
    }


    @RequestMapping("/login/authentication")
    @ResponseBody
    public Result authentication(String hostname, String username, String password){
//        boolean f = sshUtil.login();
        SSHClientUtils.init();
        if(true)
            return new Result("77243280", "0", "成功");
        return new Result("77243280", "1", "失败");
    }

}
