package com.chenpeiyu.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chenpeiyu.common.R;
import com.chenpeiyu.entity.User;
import com.chenpeiyu.service.EmailService;
import com.chenpeiyu.service.UserService;
import com.chenpeiyu.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    //操作邮箱的service
    @Autowired
    private EmailService emailService;

    //操作redis的工具类
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) {
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
            //调用瑞吉外卖的邮箱验证码
            emailService.sendVerificationCode(phone, code);
            //需要将生成的验证码保存到Session
            //request.getSession().setAttribute(phone, code);
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }

    //1). 获取前端传递的手机号和验证码
    //
    //2). 从Session中获取到手机号对应的正确的验证码
    //
    //3). 进行验证码的比对 , 如果比对失败, 直接返回错误信息
    //
    //4). 如果比对成功, 需要根据手机号查询当前用户, 如果用户不存在, 则自动注册一个新用户
    //
    //5). 将登录用户的ID存储Session中
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpServletRequest request) {
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
//        //从Session中获取保存的验证码
//        Object codeInSession = request.getSession().getAttribute(phone);

        //从redis中取出验证码
        Object codeInRedis = redisTemplate.opsForValue().get(phone);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if (codeInRedis != null && codeInRedis.equals(code)) {
            //如果能够比对成功，说明登录成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }

            request.getSession().setAttribute("user", user.getId());
            //登录成功之后，直接将redis当中的key直接删除了
            redisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登录失败");
    }


    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request) {
        //清理session中的用户id
        request.getSession().removeAttribute("user");
        return R.success("退出成功！");
    }

    @GetMapping("/getName")
    public R<User> getName(@RequestBody User user) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getPhone, user.getPhone());
        User one = userService.getOne(userLambdaQueryWrapper);
        return R.success(one);
    }
}
