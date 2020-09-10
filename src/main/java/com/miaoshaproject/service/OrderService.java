package com.miaoshaproject.service;

import com.miaoshaproject.dataobject.Order;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

public interface OrderService {
    void createOrderWithMQ(Integer userId, Integer itemId, Integer amount);
    OrderModel createOrder(Integer userId, Integer itemId, Integer amount)throws BusinessException, Exception;
}
