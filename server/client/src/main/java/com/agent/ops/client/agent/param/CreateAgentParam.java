package com.agent.ops.client.agent.param;

import com.agent.ops.client.agent.dto.AssemblySnapshotDTO;
import com.agent.ops.facade.request.CommonRequest;

import java.util.List;

public class CreateAgentParam extends CommonRequest {
    public String spaceCode;
    public String name;
    public String displayName;
    public String description;
    public List<String> tags;
    public String remark;
    public String versionNo;
    public AssemblySnapshotDTO initialAssembly;
}
