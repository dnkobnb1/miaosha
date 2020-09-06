package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.Sequence;

public interface SequenceMapper {
    int insert(Sequence record);

    int insertSelective(Sequence record);

    Sequence selectByPrimaryKey(String name);

    int updateByPrimaryKeySelective(Sequence record);

    int updateByPrimaryKey(Sequence record);

    Sequence getSequenceByName(String name);

    void setCurrentValue(Integer currentValue,String name);
}