package com.agent.ops.client.agent.vo;

import java.util.ArrayList;
import java.util.List;

public class PrePublishCheckVO {
    public Boolean passed;
    public List<Item> errors = new ArrayList<>();
    public List<Item> warnings = new ArrayList<>();

    public static class Item {
        public String field;
        public String code;
        public String message;
    }
}
