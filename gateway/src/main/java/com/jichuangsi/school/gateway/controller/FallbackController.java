package com.jichuangsi.school.gateway.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jichuangsi.microservice.common.constant.ResultCode;
import com.jichuangsi.school.gateway.model.ResponseModel;

import reactor.core.publisher.Mono;

@RestController
public class FallbackController {
	protected final Log log = LogFactory.getLog(this.getClass());

	@RequestMapping("/hystrixTimeout")
	public Mono<ResponseModel> hystrixTimeout() {
		log.error("触发了断路由");
		return Mono.just(new ResponseModel(ResultCode.SYS_BUSY, ResultCode.SYS_BUSY_MSG));
	}

}
