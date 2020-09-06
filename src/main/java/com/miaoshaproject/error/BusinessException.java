package com.miaoshaproject.error;


//包装器异常异常类实现
public class BusinessException extends Exception implements CommonError {

    private CommonError commonError;

    //直接接收BusinessError的传参用于构造业务异常
    public BusinessException(CommonError commonError){
        super();
        this.commonError=commonError;
    }

    public BusinessException(CommonError commonError,String errMsg){
        super();
        this.commonError=commonError;
        this.setErrorMsg(errMsg);
    }

    @Override
    public int getErrorCode() {
        return commonError.getErrorCode();
    }

    @Override
    public String getErrorMsg() {
        return commonError.getErrorMsg();
    }

    @Override
    public CommonError setErrorMsg(String errorMsg) {
        return commonError.setErrorMsg(errorMsg);
    }
}
