/**
 * 拦截token将用户信息放入model，及总异常拦截
 */
package com.jichuangsi.school.classinteraction.controller.advice;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jichuangsi.microservice.common.model.ResponseModel;
import com.jichuangsi.microservice.common.model.UserInfoForToken;

/**
 * @author huangjiajun
 *
 */
@RestControllerAdvice
public class CommonControllerAdvice {
	
	@Value("${custom.token.userClaim}")
	private String userClaim;

	@ModelAttribute
	public void translateHeader(@RequestHeader @Nullable String userInfo, @RequestHeader @Nullable String accessToken,
			Model model) throws UnsupportedEncodingException {
		if (!StringUtils.isEmpty(accessToken)) {
			DecodedJWT jwt = JWT.decode(accessToken);
			String user = jwt.getClaim(userClaim).asString();
			model.addAttribute(userClaim, JSONObject.parseObject(user,UserInfoForToken.class));
		}
	}

	@ExceptionHandler
	public ResponseModel<Object> handler(Exception e) {
		return ResponseModel.fail("", e.getMessage());

	}

}