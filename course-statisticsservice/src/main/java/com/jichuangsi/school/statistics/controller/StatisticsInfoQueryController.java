/**
 * 统计信息查询接口
 */
package com.jichuangsi.school.statistics.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jichuangsi.microservice.common.model.ResponseModel;
import com.jichuangsi.microservice.common.model.UserInfoForToken;
import com.jichuangsi.school.statistics.model.CourseStatisticsModel;
import com.jichuangsi.school.statistics.model.QuestionStatisticsListModel;
import com.jichuangsi.school.statistics.service.ICourseStatisticsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author huangjiajun
 *
 */
@RestController
@Api("StatisticsInfoQueryController相关的api")
public class StatisticsInfoQueryController {

	@Resource
	private ICourseStatisticsService courseStatisticsService;

	@ApiOperation(value = "根据课堂id查询课堂统计信息", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "header", name = "accessToken", value = "用户token", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "path", name = "courseId", value = "课堂ID", required = true, dataType = "String") })
	@GetMapping("/getCourseStatistics/{courseId}")
	public ResponseModel<CourseStatisticsModel> getCourseStatistics(@PathVariable String courseId,
			@ModelAttribute @ApiIgnore UserInfoForToken userInfo) {
		// todo根据用户信息做权限校验，写在这里，可以做拦截

		CourseStatisticsModel model = courseStatisticsService.getCourseStatistics(courseId);
		return ResponseModel.sucess("", model);
	}

	@ApiOperation(value = "根据课堂id查询课堂中题目的统计信息", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "header", name = "accessToken", value = "用户token", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "path", name = "courseId", value = "课堂ID", required = true, dataType = "String") })
	@GetMapping("/getQuestionStatisticsList/{courseId}")
	public ResponseModel<QuestionStatisticsListModel> getQuestionStatisticsList(@PathVariable String courseId,
			@ModelAttribute @ApiIgnore UserInfoForToken userInfo) {
		// todo根据用户信息做权限校验，不一定写在这里，可以做拦截

		QuestionStatisticsListModel model = courseStatisticsService.getQuestionStatisticsList(courseId);
		return ResponseModel.sucess("", model);
	}

}
