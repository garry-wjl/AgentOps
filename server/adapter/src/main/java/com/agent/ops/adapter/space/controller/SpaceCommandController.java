package com.agent.ops.adapter.space.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.space.SpaceCommandService;
import com.agent.ops.client.space.dto.SpaceDTO;
import com.agent.ops.client.space.param.ChangeMemberRoleParam;
import com.agent.ops.client.space.param.CreateSpaceParam;
import com.agent.ops.client.space.param.DeleteSpaceParam;
import com.agent.ops.client.space.param.JoinMemberParam;
import com.agent.ops.client.space.param.RemoveMemberParam;
import com.agent.ops.client.space.param.UpdateSpaceParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 空间写操作控制器：CRUD + 成员加入/移除/改角色。
 */
@RestController
@RequestMapping("/api/spaces")
public class SpaceCommandController extends BaseController {
    /**
     * 空间写用例应用服务。
     */
    @Resource
    private SpaceCommandService spaceCommandService;

    /**
     * 创建空间。
     *
     * @param param 创建空间入参
     * @return 空间 DTO
     */
    @PostMapping("/create")
    public Result<SpaceDTO> create(@RequestBody @Valid CreateSpaceParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(spaceCommandService.create(param));
    }

    /**
     * 修改空间基础信息。
     *
     * @param param 修改空间入参
     * @return 空间 DTO
     */
    @PostMapping("/update-basic")
    public Result<SpaceDTO> updateBasic(@RequestBody @Valid UpdateSpaceParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(spaceCommandService.updateBasic(param));
    }

    /**
     * 软删除空间（确认输入式）。
     *
     * @param param 删除空间入参
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Valid DeleteSpaceParam param) {
        param.setOperatorCode(getCurrentUserCode());
        spaceCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }

    /**
     * 加入空间成员。
     *
     * @param param 加入成员入参
     * @return 空间 DTO
     */
    @PostMapping("/add-member")
    public Result<SpaceDTO> addMember(@RequestBody @Valid JoinMemberParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(spaceCommandService.addMember(param));
    }

    /**
     * 移除空间成员。
     *
     * @param param 移除成员入参
     * @return 空间 DTO
     */
    @PostMapping("/remove-member")
    public Result<SpaceDTO> removeMember(@RequestBody @Valid RemoveMemberParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(spaceCommandService.removeMember(param));
    }

    /**
     * 修改成员角色。
     *
     * @param param 修改成员角色入参
     * @return 空间 DTO
     */
    @PostMapping("/change-member-role")
    public Result<SpaceDTO> changeMemberRole(@RequestBody @Valid ChangeMemberRoleParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(spaceCommandService.changeMemberRole(param));
    }
}
