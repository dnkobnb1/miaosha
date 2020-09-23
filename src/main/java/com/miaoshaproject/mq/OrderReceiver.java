package com.miaoshaproject.mq;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.model.OrderModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;

@Component
@RabbitListener
public class OrderReceiver {

    @Autowired
    ItemService itemService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisTemplate redisTemplate;
    private static final Logger logger= LoggerFactory.getLogger(DelCacheReceiver.class);

    @RabbitHandler
    public void process(OrderModel orderModel){
        try {
            orderModel=orderService.createOrder(orderModel.getUserId(),orderModel.getItemId(),orderModel.getAmount());
            //将用户下单成功消息写入缓存
            redisTemplate.opsForValue().set("user_id",orderModel);
        }catch (Exception e) {
            logger.info("下单失败："+orderModel.getUserId());
        }
    }
}
