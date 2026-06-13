package com.agent.ops.domain.user.valueobject;

import cn.hutool.core.lang.Assert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户平台角色值对象。
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {
    /**
     * 管理员角色编码。
     */
    public static final String ADMIN_CODE = "ADMIN";

    /**
     * 普通成员角色编码。
     */
    public static final String MEMBER_CODE = "MEMBER";

    /**
     * 角色编码允许值集合。
     */
    private static final Set<String> VALID_CODES = Arrays.stream(new String[]{ADMIN_CODE, MEMBER_CODE}).collect(Collectors.toSet());

    /**
     * 角色编码。
     */
    private String code;

    /**
     * 角色展示名称。
     */
    private String label;

    /**
     * 创建管理员角色值对象。
     *
     * @return 管理员角色值对象
     */
    public static UserRole admin() {
        UserRole role = new UserRole();
        role.code = ADMIN_CODE;
        role.label = "管理员";
        return role;
    }

    /**
     * 创建普通成员角色值对象。
     *
     * @return 普通成员角色值对象
     */
    public static UserRole member() {
        UserRole role = new UserRole();
        role.code = MEMBER_CODE;
        role.label = "普通成员";
        return role;
    }

    /**
     * 判断当前角色是否为管理员角色。
     *
     * @return 如果当前角色为管理员角色则返回 true
     */
    public boolean isAdmin() {
        return ADMIN_CODE.equals(code);
    }

    /**
     * 校验角色编码是否属于内置角色范围。
     */
    public void validate() {
        Assert.notBlank(code, "用户角色编码不能为空");
        Assert.isTrue(VALID_CODES.contains(code), "用户角色编码不合法");
    }



    /**
     * 返回code。
     *
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回label。
     *
     * @return label
     */
    public String getLabel() {
        return label;
    }

    /**
     * 设置code。
     *
     * @param code code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 设置label。
     *
     * @param label label
     */
    public void setLabel(String label) {
        this.label = label;
    }
}