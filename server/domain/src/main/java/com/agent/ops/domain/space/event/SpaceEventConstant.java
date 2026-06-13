package com.agent.ops.domain.space.event;

/**
 * 空间领域事件类型常量。
 */
public final class SpaceEventConstant {
    /**
     * 空间创建事件类型。
     */
    public static final String SPACE_CREATED = "space.space.created";

    /**
     * 空间删除事件类型。
     */
    public static final String SPACE_DELETED = "space.space.deleted";

    /**
     * 空间成员加入事件类型。
     */
    public static final String SPACE_MEMBER_ADDED = "space.member.added";

    /**
     * 空间成员移除事件类型。
     */
    public static final String SPACE_MEMBER_REMOVED = "space.member.removed";

    /**
     * 空间成员角色变更事件类型。
     */
    public static final String SPACE_MEMBER_ROLE_CHANGED = "space.member.role_changed";

    private SpaceEventConstant() {
    }
}
