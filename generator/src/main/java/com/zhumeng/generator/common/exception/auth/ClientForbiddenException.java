package com.zhumeng.generator.common.exception.auth;

import com.zhumeng.generator.common.constant.CommonConstants;
import com.zhumeng.generator.common.exception.BaseException;

/**
 * Created by ace on 2017/9/12.
 */
public class ClientForbiddenException extends BaseException {
    public ClientForbiddenException(String message) {
        super(message, CommonConstants.EX_CLIENT_FORBIDDEN_CODE);
    }

}
