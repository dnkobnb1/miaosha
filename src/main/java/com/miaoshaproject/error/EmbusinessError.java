package com.miaoshaproject.error;

public enum EmbusinessError implements CommonError {

    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002,"未知错误"),
    USER_NOT_EXIST(20001,"用户不存在"),
    USER_NOT_LOGIN(20002,"用户未登陆"),
    USER_LOGIN_FAIL(20003,"用户手机号或密码不正确"),
    ITEM_NOT_EXIST(300003,"商品不存在"),
    STOCK_NOT_ENOUGH(300001,"库存不足"),
    FLOW_LIMITING(300002,"秒杀失败，当前参与人数过多");
    EmbusinessError(int errCode, String errMsg) {
        this.errCode=errCode;
        this.errMsg=errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrorCode() {
        return this.errCode;
    }

    @Override
    public String getErrorMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrorMsg(String errorMsg) {
        this.errMsg=errorMsg;
        return this;
    }
}
