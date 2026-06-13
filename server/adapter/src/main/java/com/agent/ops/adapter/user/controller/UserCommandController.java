package com.agent.ops.adapter.user.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.client.user.param.AssignRolesParam;
import com.agent.ops.client.user.param.CreateUserParam;
import com.agent.ops.client.user.param.ResetPasswordParam;
import com.agent.ops.client.user.param.SaveUserParam;
import com.agent.ops.client.user.param.UserActionParam;
import com.agent.ops.client.user.vo.UserVO;
import com.agent.ops.application.user.UserCommandService;
import com.agent.ops.client.user.dto.AssignUserRolesParamDTO;
import com.agent.ops.client.user.dto.ResetPasswordParamDTO;
import com.agent.ops.client.user.dto.UserActionParamDTO;
import com.agent.ops.client.user.dto.UserCreateParamDTO;
import com.agent.ops.client.user.dto.UserDTO;
import com.agent.ops.client.user.dto.UserSaveParamDTO;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户写操作控制器。
 */
@RestController
@RequestMapping("/api/users")
public class UserCommandController extends BaseController {
    /**
     * 用户写用例应用服务。
     */
    @Resource
    private UserCommandService userCommandService;

    /**
     * 新增用户草稿。
     *
     * @param param 创建用户参数
     * @return 用户视图对象
     */
    @PostMapping("/create")
    public Result<UserVO> create(@RequestBody @Valid CreateUserParam param) {
        UserCreateParamDTO dto = new UserCreateParamDTO();
        dto.email = param.email;
        dto.phone = param.phone;
        dto.name = param.name;
        dto.roles = param.roles;
        dto.remark = param.remark;
        dto.operatorCode = getCurrentUserCode();
        UserDTO result = userCommandService.create(dto);
        return Result.ok(toUserVO(result));
    }

    /**
     * 保存草稿用户资料。
     *
     * @param param 保存用户资料参数
     * @return 用户视图对象
     */
    @PostMapping("/save")
    public Result<UserVO> save(@RequestBody @Valid SaveUserParam param) {
        UserSaveParamDTO dto = new UserSaveParamDTO();
        dto.userNum = param.userNum;
        dto.email = param.email;
        dto.phone = param.phone;
        dto.name = param.name;
        dto.roles = param.roles;
        dto.remark = param.remark;
        dto.operatorCode = getCurrentUserCode();
        UserDTO result = userCommandService.save(dto);
        return Result.ok(toUserVO(result));
    }

    /**
     * 提交草稿用户。
     *
     * @param param 用户操作参数
     * @return 操作结果
     */
    @PostMapping("/submit")
    public Result<Boolean> submit(@RequestBody @Valid UserActionParam param) {
        UserActionParamDTO dto = new UserActionParamDTO();
        dto.userNum = param.userNum;
        dto.operatorCode = getCurrentUserCode();
        userCommandService.submit(dto);
        return Result.ok(Boolean.TRUE);
    }

    /**
     * 删除草稿用户。
     *
     * @param param 用户操作参数
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Valid UserActionParam param) {
        UserActionParamDTO dto = new UserActionParamDTO();
        dto.userNum = param.userNum;
        dto.operatorCode = getCurrentUserCode();
        userCommandService.delete(dto);
        return Result.ok(Boolean.TRUE);
    }

    /**
     * 启用用户。
     *
     * @param param 用户操作参数
     * @return 操作结果
     */
    @PostMapping("/enable")
    public Result<Boolean> enable(@RequestBody @Valid UserActionParam param) {
        UserActionParamDTO dto = new UserActionParamDTO();
        dto.userNum = param.userNum;
        dto.operatorCode = getCurrentUserCode();
        userCommandService.enable(dto);
        return Result.ok(Boolean.TRUE);
    }

    /**
     * 禁用用户。
     *
     * @param param 用户操作参数
     * @return 操作结果
     */
    @PostMapping("/disable")
    public Result<Boolean> disable(@RequestBody @Valid UserActionParam param) {
        UserActionParamDTO dto = new UserActionParamDTO();
        dto.userNum = param.userNum;
        dto.operatorCode = getCurrentUserCode();
        userCommandService.disable(dto);
        return Result.ok(Boolean.TRUE);
    }

    /**
     * 重置用户密码。
     *
     * @param param 重置密码参数
     * @return 操作结果
     */
    @PostMapping("/reset-password")
    public Result<Boolean> resetPassword(@RequestBody @Valid ResetPasswordParam param) {
        ResetPasswordParamDTO dto = new ResetPasswordParamDTO();
        dto.userNum = param.userNum;
        dto.newPassword = param.newPassword;
        dto.confirmPassword = param.confirmPassword;
        dto.operatorCode = getCurrentUserCode();
        userCommandService.resetPassword(dto);
        return Result.ok(Boolean.TRUE);
    }

    /**
     * 分配用户平台角色。
     *
     * @param param 分配角色参数
     * @return 用户视图对象
     */
    @PostMapping("/assign-roles")
    public Result<UserVO> assignRoles(@RequestBody @Valid AssignRolesParam param) {
        AssignUserRolesParamDTO dto = new AssignUserRolesParamDTO();
        dto.userNum = param.userNum;
        dto.roles = param.roles;
        dto.operatorCode = getCurrentUserCode();
        UserDTO result = userCommandService.assignRoles(dto);
        return Result.ok(toUserVO(result));
    }

    /**
     * 将用户数据传输对象转换为用户视图对象。
     *
     * @param dto 用户数据传输对象
     * @return 用户视图对象
     */
    private UserVO toUserVO(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.id = dto.id;
        vo.num = dto.num;
        vo.email = dto.email;
        vo.phone = dto.phone;
        vo.name = dto.name;
        vo.roles = dto.roles;
        vo.status = dto.status;
        vo.remark = dto.remark;
        return vo;
    }
}