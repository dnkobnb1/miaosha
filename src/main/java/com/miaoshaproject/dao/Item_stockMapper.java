package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.Item_stock;

public interface Item_stockMapper {
    int insert(Item_stock record);

    int insertSelective(Item_stock record);

    Item_stock selectByItemId(Integer id);

    int updateByPrimaryKeySelective(Item_stock record);

    int updateByPrimaryKey(Item_stock record);

    int decreaseStock(Integer itemId,Integer amount);
}