package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.Item;

import java.util.List;

public interface ItemMapper {
    int insert(Item record);

    int insertSelective(Item record);

    Item selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Item record);

    int updateByPrimaryKey(Item record);

    List<Item> listItem();

    int increaseSales(Integer id,Integer amount);
}