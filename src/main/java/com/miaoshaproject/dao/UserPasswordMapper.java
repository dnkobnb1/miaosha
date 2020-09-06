package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.UserPassword;

public interface UserPasswordMapper {
    UserPassword selectByUserId(Integer userId);

    int insert(UserPassword record);

    int insertSelective(UserPassword record);

    UserPassword selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserPassword record);

    int updateByPrimaryKey(UserPassword record);
}