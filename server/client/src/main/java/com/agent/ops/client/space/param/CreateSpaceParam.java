package com.agent.ops.client.space.param;

import com.agent.ops.facade.request.CommonRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建空间入参。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CreateSpaceParam extends CommonRequest {
    /**
     * 空间名称，1~50 字符且全局唯一。
     */
    public String name;

    /**
     * 描述。
     */
    public String description;

    /**
     * Logo URL。
     */
    public String iconUrl;
}
