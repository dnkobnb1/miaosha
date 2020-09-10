package com.miaoshaproject.mq;

import com.miaoshaproject.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "delCache")
public class DelCacheReceiver {

    private static final Logger logger= LoggerFactory.getLogger(DelCacheReceiver.class);

    @Autowired
    private ItemService itemService;

    @RabbitHandler
    public void process(String message){
        logger.info("开始删除缓存!");
        itemService.deleteItemCache(Integer.parseInt(message));
    }
}
