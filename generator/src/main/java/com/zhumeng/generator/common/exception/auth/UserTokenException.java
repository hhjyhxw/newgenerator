package com.github.wxiaoqi.security.common.exception.auth;


import com.zhumeng.generator.common.constant.CommonConstants;
import com.zhumeng.generator.common.exception.BaseException;

/**
 * Created by ace on 2017/9/8.
 */
public class UserTokenException extends BaseException {
    public UserTokenException(String message) {
        super(message, CommonConstants.EX_USER_INVALID_CODE);
    }
}
