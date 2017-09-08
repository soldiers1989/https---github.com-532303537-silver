package org.silver.shop.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.subject.Subject;
import org.silver.common.LoginType;
import org.silver.shiro.CustomizedToken;
import org.silver.shop.service.TestService;
import org.silver.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final String USER_LOGIN_TYPE = LoginType.USER.toString();

   
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("account") String email, @RequestParam("password") String password) {
        Subject currentUser = SecurityUtils.getSubject();
      
        currentUser.logout();
        if (!currentUser.isAuthenticated()) {
            CustomizedToken customizedToken = new CustomizedToken(email, password, USER_LOGIN_TYPE);
            customizedToken.setRememberMe(false);
            try {
                currentUser.login(customizedToken);
                return "redirect:http://www.baidu.com";
            } catch (IncorrectCredentialsException ice) {
                System.out.println("账号/密码不匹配！");
            } catch (LockedAccountException lae) {
                System.out.println("账户已被冻结！");
            } catch (AuthenticationException ae) {
                System.out.println(ae.getMessage());
            }
        }
        return "login_error";
    }
    
    
}
