package com.agent.ops.domain.prompt.factory;

import com.agent.ops.domain.prompt.PromptAggregate;

public interface PromptFactory {
    PromptAggregate create(String spaceCode, String name, String key, String content, String remark);
    PromptAggregate createByNum(String num);
}
