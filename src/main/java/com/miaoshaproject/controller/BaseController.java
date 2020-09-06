package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmbusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class BaseController {
    //定义exceptionhandlder
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception ex){
        Map<String, Object> responseData;
        if(ex instanceof BusinessException) {
            BusinessException businessException = (BusinessException) ex;
            responseData = new HashMap<>();
            responseData.put("errCode", businessException.getErrorCode());
            responseData.put("errMsg", businessException.getErrorMsg());
        }else {
            responseData = new HashMap<>();
            responseData.put("errCode", EmbusinessError.UNKNOWN_ERROR.getErrorCode());
            responseData.put("errMsg", EmbusinessError.UNKNOWN_ERROR.getErrorMsg());
        }
        ex.printStackTrace();
        return CommonReturnType.create(responseData, "fail");
    }
}
