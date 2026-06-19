package com.agent.ops.adapter.system.controller;

import cn.hutool.core.date.DateUtil;
import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.system.query.AuditLogQueryService;
import com.agent.ops.client.system.dto.AuditLogDTO;
import com.agent.ops.client.system.param.AuditLogQueryParam;
import com.agent.ops.facade.common.Result;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 审计日志读控制器。
 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogQueryController extends BaseController {
    @Resource
    private AuditLogQueryService auditLogQueryService;

    @GetMapping("/page")
    public Result<PageResult<AuditLogDTO>> page(@RequestParam(value = "module", required = false) String module,
                                                @RequestParam(value = "action", required = false) String action,
                                                @RequestParam(value = "operatorCode", required = false) String operatorCode,
                                                @RequestParam(value = "targetNum", required = false) String targetNum,
                                                @RequestParam(value = "from", required = false) String from,
                                                @RequestParam(value = "to", required = false) String to,
                                                @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        AuditLogQueryParam param = new AuditLogQueryParam();
        param.setOperatorCode(getCurrentUserCode());
        param.module = module;
        param.action = action;
        param.operatorCodeFilter = operatorCode;
        param.targetNum = targetNum;
        param.fromTime = parseTime(from);
        param.toTime = parseTime(to);
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNo);
        pageQuery.setPageSize(pageSize);
        param.pageQuery = pageQuery;
        return Result.ok(auditLogQueryService.page(param));
    }

    /**
     * 解析时间字符串。
     *
     * @param s 时间字符串
     * @return LocalDateTime
     */
    private LocalDateTime parseTime(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return DateUtil.parse(s).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
}
