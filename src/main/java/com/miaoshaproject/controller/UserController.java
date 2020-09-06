package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewObject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.CommonError;
import com.miaoshaproject.error.EmbusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/login",method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name="telephone")String telephone,
                                  @RequestParam(name = "password")String password,
                                  HttpSession httpSession) throws Exception {
        //入参效验
        if(StringUtils.isEmpty(telephone)||StringUtils.isEmpty(password)){
            throw new BusinessException(EmbusinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserModel userModel=userService.validateLogin(telephone,EncodeByMd5(password));
        request.getSession().setAttribute("IS_LOGIN",true);
        request.getSession().setAttribute("LOGIN_USER",userModel);
        return CommonReturnType.create(null);
    }

    @RequestMapping(value = "/register",method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType register(@RequestParam String name,@RequestParam Integer age,
                                     @RequestParam String telephone,@RequestParam Byte gender,
                                     @RequestParam String password,@RequestParam String otpcode,
                                     HttpSession httpSession)throws Exception
    {
        String insessionOtpCode=(String)httpSession.getAttribute(telephone);
        if(insessionOtpCode==null||!insessionOtpCode.equals(otpcode)){
            throw new BusinessException(EmbusinessError.PARAMETER_VALIDATION_ERROR,"验证码错误");
        }
        UserModel userModel=new UserModel();
        userModel.setName(name);
        userModel.setTelephone(telephone);
        userModel.setEncryptPassword(EncodeByMd5(password));
        userModel.setAge(age);
        userModel.setGender(gender);
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    private String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder=new BASE64Encoder();
        String newstr=base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam String telephone,HttpSession httpSession){
        //需要按照规则生成OTP验证码
        Random random=new Random();
        int randomint=random.nextInt(99999);
        randomint+=10000;
        String otpCode=String.valueOf(randomint);
        //将OTP验证码同对应用户手机号关联
        httpSession.setAttribute(telephone,otpCode);
        //将OTP验证码发送给用户(省略)
        System.out.println(otpCode);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id)throws BusinessException{
        UserModel userModel=userService.getUserById(id);
        if(userModel==null){
            throw new BusinessException(EmbusinessError.USER_NOT_EXIST);
        }
        UserVO userVO=convertFromModel(userModel);
        //将核心领域模型对象转换为对应id的用户对象
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel){
        if(userModel==null)return null;
        UserVO userVO=new UserVO();
        //字段名一致，数据类型一致才能拷贝
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

}
