
package com.zhumeng.generator.msg.auth;

import com.zhumeng.generator.common.constant.RestCodeConstants;
import com.zhumeng.generator.msg.BaseResponse;

/**
 * Created by ace on 2017/8/23.
 */
public class TokenErrorResponse extends BaseResponse {
    public TokenErrorResponse(String message) {
        super(RestCodeConstants.TOKEN_ERROR_CODE, message);
    }
}
