package com.agent.ops.client.skill.param;

import com.agent.ops.client.skill.enums.FileType;
import com.agent.ops.facade.request.CommonRequest;

public class CreateResourceFileParam extends CommonRequest {
    public String versionCode;
    public String path;
    public FileType type;
    public String content;
}
