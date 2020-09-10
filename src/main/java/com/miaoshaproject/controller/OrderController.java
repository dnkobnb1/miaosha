package com.miaoshaproject.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.miaoshaproject.dataobject.User;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmbusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.CacheService;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

@Controller
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
@RequestMapping("/order")
public class OrderController extends BaseController {

    @Autowired
    OrderService orderService;

    @Autowired
    ItemService itemService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    //使用令牌桶限流，每秒只能放行50个请求
    RateLimiter rateLimiter=RateLimiter.create(50);

    @RequestMapping(value = "/createorder",method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam Integer itemId, @RequestParam Integer amount, HttpSession httpSession) throws Exception {
        // 非阻塞式获取令牌,等待1000ms，如果还是拿不到令牌，返回失败
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            throw new BusinessException(EmbusinessError.FLOW_LIMITING,"参与人数过多，秒杀失败");
        }
        UserModel userModel=(UserModel) request.getSession().getAttribute("LOGIN_USER");
        if(userModel==null)
            throw new BusinessException(EmbusinessError.USER_NOT_LOGIN,"还未登陆，无法下单");
        ItemModel itemModel=itemService.getItemByIdInCache(itemId);
        if(itemModel.getStock()<amount)
            throw new BusinessException(EmbusinessError.STOCK_NOT_ENOUGH,"库存不足,无法下单");

        //使用消息队列完成异步下单
        orderService.createOrderWithMQ(userModel.getId(),itemId,amount);
        //直接下单方式
        //OrderModel orderModel=orderService.createOrder(userModel.getId(),itemId,amount);
        //完成下单操作后删除缓存
        //itemService.deleteItemCache(itemId);
        return CommonReturnType.create(null);
    }
}
