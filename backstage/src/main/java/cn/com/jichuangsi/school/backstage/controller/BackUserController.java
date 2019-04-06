package cn.com.jichuangsi.school.backstage.controller;

import cn.com.jichuangsi.school.backstage.exception.BackUserException;
import cn.com.jichuangsi.school.backstage.feign.model.SchoolModel;
import cn.com.jichuangsi.school.backstage.model.BackUserModel;
import cn.com.jichuangsi.school.backstage.model.UpdatePwdModel;
import cn.com.jichuangsi.school.backstage.service.IBackUserService;
import com.jichuangsi.microservice.common.model.ResponseModel;
import com.jichuangsi.microservice.common.model.UserInfoForToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/back/user")
@Api("后台登录,注册，修改密码，注销账户")
public class BackUserController {

    @Resource
    private IBackUserService backUserService;

    @ApiOperation(value = "注册后台用户", notes = "")
    @ApiImplicitParams({})
    @PostMapping(value = "/registAccount")
    public ResponseModel registAccount(@Validated @RequestBody BackUserModel model){
        try {
            backUserService.registBackUser(model);
        } catch (BackUserException e) {
            return ResponseModel.fail("",e.getMessage());
        }
        return ResponseModel.sucessWithEmptyData("");
    }

    @ApiOperation(value = "后台登录", notes = "")
    @ApiImplicitParams({})
    @PostMapping(value = "/login")
    public ResponseModel<String> backStageLogin(@RequestBody BackUserModel model){
        try {
            return ResponseModel.sucess("",backUserService.loginBackUser(model));
        } catch (BackUserException e) {
            return ResponseModel.fail("",e.getMessage());
        }
    }

    @ApiOperation(value = "后台注销账户", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", name = "accessToken", value = "用户token", required = true, dataType = "String")
    })
    @DeleteMapping(value = "/deleteAccount")
    public ResponseModel deleteAccount(@ModelAttribute UserInfoForToken userInfo,@RequestBody BackUserModel model){
        try {
            backUserService.deleteBackUser(userInfo, model);
        } catch (BackUserException e) {
            return ResponseModel.fail("",e.getMessage());
        }
        return ResponseModel.sucessWithEmptyData("");
    }

    @ApiOperation(value = "后台本人修改账号密码", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", name = "accessToken", value = "用户token", required = true, dataType = "String")
    })
    @PostMapping(value = "/updatePwd")
    public ResponseModel updatePwd(@ModelAttribute UserInfoForToken userInfo, @Validated @RequestBody UpdatePwdModel model){
        try {
            backUserService.updateBackUserPwd(userInfo, model);
        } catch (BackUserException e) {
            return ResponseModel.fail("",e.getMessage());
        }
        return ResponseModel.sucessWithEmptyData("");
    }

    @ApiOperation(value = "查询后台所有登记内非删除学校", notes = "")
    @ApiImplicitParams({})
    @GetMapping(value = "/getBackSchools")
    public ResponseModel<List<SchoolModel>> getBackSchools(){
        try {
            return ResponseModel.sucess("",backUserService.getBackSchools());
        } catch (BackUserException e) {
            return ResponseModel.fail("",e.getMessage());
        }
    }
}
