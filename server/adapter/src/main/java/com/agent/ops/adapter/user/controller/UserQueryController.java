package com.agent.ops.adapter.user.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.client.user.param.UserDetailParam;
import com.agent.ops.client.user.param.UserPageParam;
import com.agent.ops.client.user.vo.UserPageVO;
import com.agent.ops.client.user.vo.UserRoleVO;
import com.agent.ops.client.user.vo.UserVO;
import com.agent.ops.application.user.UserQueryService;
import com.agent.ops.client.user.dto.PageResultDTO;
import com.agent.ops.client.user.dto.RoleOptionsParamDTO;
import com.agent.ops.client.user.dto.UserDTO;
import com.agent.ops.client.user.dto.UserDetailParamDTO;
import com.agent.ops.client.user.dto.UserPageParamDTO;
import com.agent.ops.client.user.dto.UserRoleDTO;
import com.agent.ops.client.user.dto.UserRoleQueryParamDTO;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户读操作控制器。
 */
@RestController
@RequestMapping("/api/users")
public class UserQueryController extends BaseController {
    /**
     * 用户读用例应用服务。
     */
    @Resource
    private UserQueryService userQueryService;

    /**
     * 分页查询用户列表。
     *
     * @param param 用户分页查询参数
     * @return 用户分页视图对象
     */
    @GetMapping("/page")
    public Result<UserPageVO> page(UserPageParam param) {
        UserPageParamDTO dto = new UserPageParamDTO();
        dto.keyword = param.keyword;
        dto.status = param.status;
        dto.role = param.role;
        dto.pageNo = param.pageNo;
        dto.pageSize = param.pageSize;
        dto.operatorId = getCurrentUserId();
        PageResultDTO<UserDTO> result = userQueryService.page(dto);
        UserPageVO vo = new UserPageVO();
        vo.total = result.total;
        vo.pageNo = result.pageNo;
        vo.pageSize = result.pageSize;
        vo.records = toUserVOList(result.records);
        return Result.ok(vo);
    }

    /**
     * 查询用户详情。
     *
     * @param param 用户详情查询参数
     * @return 用户视图对象
     */
    @GetMapping("/detail")
    public Result<UserVO> detail(UserDetailParam param) {
        UserDetailParamDTO dto = new UserDetailParamDTO();
        dto.userNum = param.userNum;
        dto.operatorId = getCurrentUserId();
        UserDTO result = userQueryService.detail(dto);
        return Result.ok(toUserVO(result));
    }

    /**
     * 查询可分配的平台角色选项。
     *
     * @return 角色视图对象列表
     */
    @GetMapping("/roles/options")
    public Result<List<UserRoleVO>> roleOptions() {
        RoleOptionsParamDTO dto = new RoleOptionsParamDTO();
        dto.operatorId = getCurrentUserId();
        List<UserRoleDTO> result = userQueryService.roleOptions(dto);
        List<UserRoleVO> vos = new ArrayList<>();
        for (UserRoleDTO role : result) {
            UserRoleVO vo = new UserRoleVO();
            vo.code = role.code;
            vo.label = role.label;
            vos.add(vo);
        }
        return Result.ok(vos);
    }

    /**
     * 查询指定用户的平台角色。
     *
     * @param userNum 用户业务编码
     * @return 用户角色视图对象列表
     */
    @GetMapping("/roles")
    public Result<List<UserRoleVO>> userRoles(String userNum) {
        UserRoleQueryParamDTO dto = new UserRoleQueryParamDTO();
        dto.userNum = userNum;
        dto.operatorId = getCurrentUserId();
        List<UserRoleDTO> result = userQueryService.userRoles(dto);
        List<UserRoleVO> vos = new ArrayList<>();
        for (UserRoleDTO role : result) {
            UserRoleVO vo = new UserRoleVO();
            vo.code = role.code;
            vo.label = role.label;
            vos.add(vo);
        }
        return Result.ok(vos);
    }

    /**
     * 将用户数据传输对象列表转换为用户视图对象列表。
     *
     * @param dtos 用户数据传输对象列表
     * @return 用户视图对象列表
     */
    private List<UserVO> toUserVOList(List<UserDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        List<UserVO> vos = new ArrayList<>();
        for (UserDTO dto : dtos) {
            vos.add(toUserVO(dto));
        }
        return vos;
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