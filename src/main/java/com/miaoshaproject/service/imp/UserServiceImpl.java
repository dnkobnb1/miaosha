package com.miaoshaproject.service.imp;

import com.miaoshaproject.dao.UserMapper;
import com.miaoshaproject.dao.UserPasswordMapper;
import com.miaoshaproject.dataobject.User;
import com.miaoshaproject.dataobject.UserPassword;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmbusinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validate.ValidationResult;
import com.miaoshaproject.validate.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired(required = false)
    private UserPasswordMapper userPasswordMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisTemplate redisTemplate;

    public UserModel validateLogin(String telephone,String encrptPassword)throws Exception{
        //通过用户手机获取用户信息
        User user=userMapper.selectByTelephone(telephone);
        if(user==null)throw new BusinessException(EmbusinessError.USER_NOT_EXIST,"用户不存在");
        UserPassword userPassword=userPasswordMapper.selectByUserId(user.getId());
        //比对用户信息内加密的密码和传输进来的是否匹配
        UserModel userModel=convertFromDataObject(user,userPassword);
        if(!StringUtils.equals(encrptPassword,userModel.getEncryptPassword())){
            throw new BusinessException(EmbusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel= (UserModel) redisTemplate.opsForValue().get("user_validate_"+id);
        if(userModel==null){
             userModel=this.getUserById(id);
             redisTemplate.opsForValue().set("user_validate_"+id,userModel);
             redisTemplate.expire("user_validate_"+id,10, TimeUnit.MINUTES);
        }
        return userModel;
    }

    @Override
    public UserModel getUserById(Integer id) {
        User user=userMapper.selectByPrimaryKey(id);
        if(user==null)return null;
        UserPassword userPassword=userPasswordMapper.selectByUserId(id);
        return convertFromDataObject(user,userPassword);
    }

    //父类抛出异常，子类必须抛出异常范围小于等于的
    @Override
    @Transactional
    public void register(UserModel userModel)throws Exception {
        if(userModel==null){
            throw new BusinessException(EmbusinessError.PARAMETER_VALIDATION_ERROR,"输入参数不合法！");
        }
        ValidationResult result=validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmbusinessError.PARAMETER_VALIDATION_ERROR,result.getErrorMsg());
        }
        User user=convertFromModel(userModel);
        userMapper.insertSelective(user);
        userModel.setId(user.getId());
        UserPassword userPassword=new UserPassword();
        userPassword.setEncryptPassword(userModel.getEncryptPassword());
        userPassword.setUserId(userModel.getId());
        userPasswordMapper.insertSelective(userPassword);
        return ;
    }

    private UserModel convertFromDataObject(User user, UserPassword userPassword){
        if(user==null)return null;
        UserModel userModel=new UserModel();
        BeanUtils.copyProperties(user,userModel);
        if(userPassword!=null) {
            userModel.setEncryptPassword(userPassword.getEncryptPassword());
        }
        return userModel;
    }
    private User convertFromModel(UserModel userModel){
        if(userModel==null)return null;
        User user=new User();
        BeanUtils.copyProperties(userModel,user);
        return user;
    }
}
