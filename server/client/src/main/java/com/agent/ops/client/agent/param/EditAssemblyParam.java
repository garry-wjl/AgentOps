package com.agent.ops.client.agent.param;

import com.agent.ops.client.agent.dto.AssemblySnapshotDTO;
import com.agent.ops.facade.request.CommonRequest;

public class EditAssemblyParam extends CommonRequest {
    public String num;
    public AssemblySnapshotDTO snapshot;
}
