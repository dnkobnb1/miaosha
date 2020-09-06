package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewObject.ItemVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.CommonError;
import com.miaoshaproject.error.EmbusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.CacheService;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.model.ItemModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
@RequestMapping("/item")
public class ItemController extends BaseController {
    @Autowired
    ItemService itemService;

    @RequestMapping(value = "/create",method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl)throws Exception{
        ItemModel itemModel=new ItemModel();
        itemModel.setTitle(title);
        itemModel.setStock(stock);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        itemModel.setPrice(price);
        ItemModel itemModelForReturn=itemService.createItem(itemModel);
        ItemVO itemVO=convertFromItemModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }

    @RequestMapping(value = "/list",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem(){
        List<ItemModel> itemModelList=itemService.listItem();
        List<ItemVO> itemVOList=itemModelList.stream().map(itemModel -> {
            return convertFromItemModel(itemModel);
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }
    private ItemVO convertFromItemModel(ItemModel itemModel){
        if(itemModel==null)return null;
        ItemVO itemVO=new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        if(itemModel.getPromoModel()!=null){
            //有秒杀活动
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        }else itemVO.setPromoStatus(0);
        return itemVO;
    }
    @RequestMapping(value = "/get",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam Integer id){
        //根据商品id到redis内或者数据库内获取
        ItemModel itemModel = itemService.getItemByIdInCache(id);
        ItemVO itemVO=convertFromItemModel(itemModel);
        return CommonReturnType.create(itemVO);
    }
}
