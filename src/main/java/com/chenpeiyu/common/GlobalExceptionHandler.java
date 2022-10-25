package com.chenpeiyu.common;

//全局异常处理

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

//通过这个注解，指定来拦截所有有这个注解的controller
@ControllerAdvice(annotations = {RestController.class})
//让返回的东西是一个json格式的，可以被前端解析
@ResponseBody
@Slf4j
//上面的两个直接可以合并成: @RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     */
    //加上异常处理的相关注解,并指明是哪一类异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception) {
        log.error(exception.getMessage());
        if (exception.getMessage().contains("Duplicate entry")) {
            String[] split = exception.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    //自定义异常的处理方法

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException customException) {
        //捕获自定义的异常
        log.error(customException.getMessage());
        String message = customException.getMessage();
        //直接返回一个错误信息
        return R.error(message);
    }


}
