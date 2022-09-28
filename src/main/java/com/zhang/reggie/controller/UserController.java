package com.zhang.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhang.reggie.common.R;
import com.zhang.reggie.entity.User;
import com.zhang.reggie.service.UserService;
import com.zhang.reggie.utils.SMSUtils;
import com.zhang.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     *
     * @param user
     * @return
     */
    @RequestMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {//用User对象来接收phone属性
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //需要将生成的验证码保存到Session
            session.setAttribute(phone,code);
            return R.success("短信发送成功");
        }

        return R.success("短信发送失败");
    }

    /**
     *登入验证
     * @param map
     * @param session
     * @return
     * R<User> 页面/浏览器也需要保存一份用户信息 所以返回的泛型为User
     */
    @RequestMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从session中获取保存的验证码
        String codeInSession = session.getAttribute(phone).toString();
        //进行验证码的对比(页面提交的验证码和session中保存的验证码对比)
        //codeInSession.equals(code) 说明验证码对比失败
        if (codeInSession != null && codeInSession.equals(code)){
            //如果比对成功，说明登入成功 -- 如果比对失败则直接返回失败信息，无法进入下一步是否为新用户的判断
            //比对成功之后，判断当前手机号用户是否为新用户，如果是新用户自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user == null){//说明为新用户，数据库中没有改手机号的用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //登入成功，需要将用户的id存进去，否则即使登入成功，由于session没有存放该用户的id，进行访问的时候也会被过滤器过滤
            session.setAttribute("user",user.getId());
            return R.success(user);
        }


        return R.error("登入失败");
    }

}
