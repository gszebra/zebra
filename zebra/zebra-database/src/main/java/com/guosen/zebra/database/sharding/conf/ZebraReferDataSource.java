package com.guosen.zebra.database.sharding.conf;

import com.alibaba.fastjson.JSONObject;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 构成Sharding数据源的普通数据源信息
 */
public class ZebraReferDataSource {

    /**
     * 普通数据源名称集合
     */
    @NotEmpty
    @Valid
    private Set<String> names = new LinkedHashSet<>();

    public Set<String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
