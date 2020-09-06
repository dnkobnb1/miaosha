package com.miaoshaproject.service;

import com.miaoshaproject.service.model.UserModel;

public interface UserService {
    UserModel getUserById(Integer id);
    void register(UserModel userModel)throws Exception;
    UserModel validateLogin(String telephone,String password)throws Exception;
    UserModel getUserByIdInCache(Integer id);
}
