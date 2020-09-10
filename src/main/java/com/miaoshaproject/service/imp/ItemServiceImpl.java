package com.miaoshaproject.service.imp;

import com.miaoshaproject.dao.ItemMapper;
import com.miaoshaproject.dao.Item_stockMapper;
import com.miaoshaproject.dataobject.Item;
import com.miaoshaproject.dataobject.Item_stock;
import com.miaoshaproject.dataobject.Promo;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmbusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validate.ValidatorImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class  ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired(required = false)
    private ItemMapper itemMapper;

    @Autowired(required = false)
    private Item_stockMapper item_stockMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //效验参数
        if(validator.validate(itemModel).isHasErrors()){
            throw new BusinessException(EmbusinessError.PARAMETER_VALIDATION_ERROR,"参数不合法");
        }
        //转换itemmodel为dataobject
        Item item=convertFromItemModel(itemModel);
        item.setPrice(itemModel.getPrice().doubleValue());
        //写入数据库
        itemMapper.insertSelective(item);
        Item_stock item_stock=new Item_stock();
        item_stock.setItemId(item.getId());
        item_stock.setStock(itemModel.getStock());
        item_stockMapper.insertSelective(item_stock);
        return getItemById(item.getId());
    }

    //删除缓存
    @Override
    public void deleteItemCache(int id){
        boolean result=redisTemplate.delete("item_validate_"+id);
        //假设删除缓存失败，使用消息队列重试删除缓存
        if(!result){
            rabbitTemplate.convertAndSend("delCache",id);
        }
    }

    private Item convertFromItemModel(ItemModel itemModel){
        if(itemModel==null)return null;
        Item item=new Item();
        BeanUtils.copyProperties(itemModel,item);
        return item;
    }
    private ItemModel convertFromDataObject(Item item,Item_stock item_stock){
        if(item==null)return null;
        ItemModel itemModel=new ItemModel();
        BeanUtils.copyProperties(item,itemModel);
        if(item_stock!=null)
        itemModel.setStock(item_stock.getStock());
        return itemModel;
    }


    @Override
    public List<ItemModel> listItem() {
       List<Item> itemList=itemMapper.listItem();
       List<ItemModel> itemModelList=itemList.stream().map(item -> {
           Item_stock item_stock=item_stockMapper.selectByItemId(item.getId());
           ItemModel itemModel=convertFromDataObject(item,item_stock);
           return itemModel;
       }).collect(Collectors.toList());
       return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        Item item=itemMapper.selectByPrimaryKey(id);
        if(item==null)return null;
        Item_stock item_stock=item_stockMapper.selectByItemId(item.getId());
        ItemModel itemModel;
        itemModel=convertFromDataObject(item,item_stock);
        itemModel.setPrice(new BigDecimal(item.getPrice()));
        PromoModel promoModel=promoService.getPromoByItemId(itemModel.getId());
        //通过模型聚合的方式将秒杀活动聚合进来
        if(promoModel!=null&&promoModel.getStatus()!=3){
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public boolean decrease(Integer itemId,Integer amount) {
        int affectRow=item_stockMapper.decreaseStock(itemId,amount);
        if(affectRow>0){
            return true;
        } else
        return false;
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) {
        itemMapper.increaseSales(itemId,amount);
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel= (ItemModel) redisTemplate.opsForValue().get("item_validate_"+id);
        if(itemModel==null){
            itemModel=getItemById(id);
            redisTemplate.opsForValue().set("item_validate_"+id,itemModel);
            //交易验证缓存10分钟过期
            redisTemplate.expire("item_validate_"+id,10, TimeUnit.MINUTES);
        }
        return itemModel;
    }


}
