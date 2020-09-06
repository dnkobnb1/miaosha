package com.miaoshaproject.service.imp;

import com.miaoshaproject.dao.Item_stockMapper;
import com.miaoshaproject.dao.OrderMapper;
import com.miaoshaproject.dao.SequenceMapper;
import com.miaoshaproject.dataobject.Order;
import com.miaoshaproject.dataobject.Sequence;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmbusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired(required = false)
    Item_stockMapper item_stockMapper;

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private OrderMapper orderMapper;

    @Autowired(required = false)
    private SequenceMapper sequenceMapper;

    @Override
    //默认数据库默认的隔离级别，传播行为默认Propagation.REQUIRED
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws Exception {
        //校验下单状态,下单的商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel=itemService.getItemByIdInCache(itemId);
        if(itemModel==null)throw new BusinessException(EmbusinessError.PARAMETER_VALIDATION_ERROR,"商品不存在");
        UserModel userModel=userService.getUserByIdInCache(userId);
        if(userModel==null)throw new BusinessException(EmbusinessError.USER_NOT_EXIST,"用户不存在");
        if(amount<=0||amount>99)throw new BusinessException(EmbusinessError.PARAMETER_VALIDATION_ERROR,"购买数量超过限制");
        //落单减库存
        boolean result=itemService.decrease(itemId,amount);
        if(!result)throw new BusinessException(EmbusinessError.STOCK_NOT_ENOUGH,"库存不足");
        //订单入库
        OrderModel orderModel=new OrderModel();
        orderModel.setItemId(itemModel.getId());
        orderModel.setAmount(amount);
        orderModel.setItemPrice(itemModel.getPrice());
        orderModel.setUserId(userModel.getId());
        PromoModel promoModel=itemModel.getPromoModel();
        if(promoModel!=null&&promoModel.getStatus()==2&&promoModel.getEndDate().isAfterNow()){
            //秒杀活动期间
            orderModel.setOrderPrice(itemModel.getPromoModel().getPromoItemPrice().multiply(new BigDecimal(amount)));
        }else {
            orderModel.setOrderPrice(itemModel.getPrice().multiply(new BigDecimal(amount)));
        }
        orderModel.setId(generateOrderNo());
        Order order=convertFromOrderModel(orderModel);
        order.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        order.setItemPrice(orderModel.getItemPrice().doubleValue());
        //生成交易流水号
        orderMapper.insertSelective(order);
        itemService.increaseSales(itemId,amount);
        return orderModel;
    }
    //新的事务,保证sequence全局唯一性
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo(){
        //订单号有16位
        StringBuilder stringBuilder=new StringBuilder();
        //前8位为年月日
        LocalDateTime now=LocalDateTime.now();
        stringBuilder.append(now.format(DateTimeFormatter.ISO_DATE).replace("-",""));
        //中间6位自增序列
        Sequence sequence=sequenceMapper.getSequenceByName("order_info");
        sequenceMapper.setCurrentValue(sequence.getCurrentvalue()+sequence.getStep(),sequence.getName());
        String sequenceStr=sequence.getCurrentvalue().toString();
        for(int i=0;i<6-sequenceStr.length();i++){
            stringBuilder.append("0");
        }
        stringBuilder.append(sequenceStr);
        //最后2位是分库分表位
        stringBuilder.append("00");
        return stringBuilder.toString();
    }

    private Order convertFromOrderModel(OrderModel orderModel){
        if(orderModel==null)return null;
        Order order= new Order();
        BeanUtils.copyProperties(orderModel,order);
        return order;
    }
}
