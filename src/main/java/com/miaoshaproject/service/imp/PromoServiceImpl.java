package com.miaoshaproject.service.imp;

import com.miaoshaproject.dao.PromoMapper;
import com.miaoshaproject.dataobject.Promo;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired(required = false)
    private PromoMapper promoMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //获取对应商品的秒杀活动信息
        Promo promo=promoMapper.selectByItemId(itemId);
        PromoModel promoModel=convertFormDataObject(promo);
        if(promoModel==null)return null;
        //判断当前时间，秒杀活动是否未结束
        DateTime now=new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }
        else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }
        else promoModel.setStatus(2);
        return promoModel;
    }

    //发布活动
    @Override
    public void publishPromo(Integer promoId) {

    }

    private PromoModel convertFormDataObject(Promo promo){
        if(promo==null)return null;
        PromoModel promoModel=new PromoModel();
        BeanUtils.copyProperties(promo,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promo.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promo.getStartDate()));
        promoModel.setEndDate(new DateTime(promo.getEndDate()));
        return promoModel;
    }
}
