package com.agent.ops.facade.domain;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 可持久化领域实体与聚合根的基础类型。
 */
@Getter
@Setter
public abstract class DomainEntity {
    /**
     * 数据库自增主键。仅供持久化层使用，不得作为跨领域引用。
     */
    protected Long id;

    /**
     * 跨层传递和外部展示使用的业务编码，所有跨领域引用均以业务编码（String）为准。
     */
    protected String num;

    /**
     * 创建该实体的操作人业务编码。
     */
    protected String createNo;

    /**
     * 最后更新该实体的操作人业务编码。
     */
    protected String updateNo;

    /**
     * 实体创建时间。
     */
    protected LocalDateTime createTime;

    /**
     * 实体最后更新时间。
     */
    protected LocalDateTime updateTime;

    /**
     * 在持久化前初始化或刷新审计字段。
     *
     * @param operatorCode 当前操作人业务编码
     */
    public void initialize(String operatorCode) {
        Assert.notBlank(operatorCode, "operatorCode must not be blank");
        LocalDateTime now = LocalDateTimeUtil.now();
        this.createTime = this.createTime == null ? now : this.createTime;
        this.createNo = this.createNo == null ? operatorCode : this.createNo;
        this.updateTime = now;
        this.updateNo = operatorCode;
    }

    /**
     * 校验通用审计字段，并委托子类校验业务不变式。
     */
    public void validate() {
        Assert.notBlank(this.createNo, "createNo must not be blank");
        Assert.notBlank(this.updateNo, "updateNo must not be blank");
        Assert.notNull(this.createTime, "createTime must not be null");
        Assert.notNull(this.updateTime, "updateTime must not be null");
        domainValidate();
    }

    /**
     * 校验子类特有的业务不变式。
     */
    public abstract void domainValidate();

    /**
     * 持久化当前领域实体状态。
     *
     * @param operatorCode 当前操作人业务编码
     */
    public abstract void save(String operatorCode);

    /**
     * 删除或标记删除当前领域实体。
     *
     * @param operatorCode 当前操作人业务编码
     */
    public abstract void delete(String operatorCode);

    /**
     * 返回id。
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * 返回num。
     *
     * @return num
     */
    public String getNum() {
        return num;
    }

    /**
     * 返回createNo。
     *
     * @return createNo
     */
    public String getCreateNo() {
        return createNo;
    }

    /**
     * 返回updateNo。
     *
     * @return updateNo
     */
    public String getUpdateNo() {
        return updateNo;
    }

    /**
     * 返回createTime。
     *
     * @return createTime
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 返回updateTime。
     *
     * @return updateTime
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置id。
     *
     * @param id id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 设置num。
     *
     * @param num num
     */
    public void setNum(String num) {
        this.num = num;
    }

    /**
     * 设置createNo。
     *
     * @param createNo createNo
     */
    public void setCreateNo(String createNo) {
        this.createNo = createNo;
    }

    /**
     * 设置updateNo。
     *
     * @param updateNo updateNo
     */
    public void setUpdateNo(String updateNo) {
        this.updateNo = updateNo;
    }

    /**
     * 设置createTime。
     *
     * @param createTime createTime
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 设置updateTime。
     *
     * @param updateTime updateTime
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
