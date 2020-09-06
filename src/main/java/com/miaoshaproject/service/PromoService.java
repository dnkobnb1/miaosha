package com.miaoshaproject.service;

import com.miaoshaproject.dataobject.Promo;
import com.miaoshaproject.service.model.PromoModel;

public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId);
    void publishPromo(Integer promoId);
}
