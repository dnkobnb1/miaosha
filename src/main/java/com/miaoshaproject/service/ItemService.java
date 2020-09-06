package com.miaoshaproject.service;

import com.miaoshaproject.dataobject.Item;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;
import org.omg.CORBA.INTERNAL;

import java.util.List;

public interface ItemService {
    ItemModel createItem(ItemModel itemModel) throws BusinessException;
    //商品列表浏览
    List<ItemModel> listItem();
    //商品详情浏览
    ItemModel getItemById(Integer id);
    boolean decrease(Integer itemId,Integer amount);
    void increaseSales(Integer itemId,Integer amount);
    ItemModel getItemByIdInCache(Integer id);
    void deleteItemCache(int id);
}
