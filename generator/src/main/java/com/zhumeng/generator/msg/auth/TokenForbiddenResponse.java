package com.zhumeng.generator.msg.auth;

import com.zhumeng.generator.common.constant.RestCodeConstants;
import com.zhumeng.generator.msg.BaseResponse;

/**
 * Created by ace on 2017/8/25.
 */
public class TokenForbiddenResponse  extends BaseResponse {
    public TokenForbiddenResponse(String message) {
        super(RestCodeConstants.TOKEN_FORBIDDEN_CODE, message);
    }
}
