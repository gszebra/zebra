/**   
* @Title: SentinelManager.java 
* @Package com.guosen.zebra.core.monitor.health 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年11月19日 下午1:46:47 
* @version V1.0   
*/
package com.guosen.zebra.core.monitor.health;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

/**
 * @ClassName: SentinelManager
 * @Description: 限流、熔断、系统保护管理类
 * @author 邓启翔
 * @date 2018年11月19日 下午1:46:47
 * 
 */
public class SentinelManager {
	private static boolean openGatewayLimit = false;
	public synchronized static void initFlowQpsRule(JSONObject obj) {
		List<FlowRule> rules = Lists.newArrayList();
		// 普通限流
		FlowRule rule1 = new FlowRule();
		rule1.setResource(obj.getString("resource"));
		rule1.setCount(obj.getInteger("count"));
		rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rules.add(rule1);
		// 泛化调用限流
		FlowRule rule2 = new FlowRule();
		rule2.setResource(obj.getString("resourceJson"));
		rule2.setCount(obj.getInteger("count"));
		rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rules.add(rule2);
		FlowRuleManager.loadRules(rules);
	}

	public synchronized static void initDegradeRule(JSONObject obj) {
		List<DegradeRule> rules = Lists.newArrayList();
		// 普通熔断
		DegradeRule rule1 = new DegradeRule();
		rule1.setResource(obj.getString("resource"));
		// set threshold rt, 10 ms
		rule1.setCount(obj.getInteger("count"));
		rule1.setGrade(RuleConstant.DEGRADE_GRADE_RT);
		rule1.setTimeWindow(obj.getInteger("timeWindow"));
		rules.add(rule1);

		// 普通熔断
		DegradeRule rule2 = new DegradeRule();
		rule2.setResource(obj.getString("resourceJson"));
		// set threshold rt, 10 ms
		rule2.setCount(obj.getInteger("count"));
		rule2.setGrade(RuleConstant.DEGRADE_GRADE_RT);
		rule2.setTimeWindow(obj.getInteger("timeWindow"));
		rules.add(rule2);
		DegradeRuleManager.loadRules(rules);
	}

	public synchronized static void initSystemProtectionRule(JSONObject obj) {
		List<SystemRule> rules = Lists.newArrayList();
		SystemRule rule = new SystemRule();
		rule.setHighestSystemLoad(obj.getInteger("highestSystemLoad"));
		if (obj.getLong("avgRt")!=null&&obj.getLong("avgRt")>0) {
			rule.setAvgRt(obj.getLong("avgRt"));
		}
		if (obj.getLong("maxThread")!=null&&obj.getLong("maxThread")>0) {
			rule.setMaxThread(obj.getLong("maxThread"));
		}
		rules.add(rule);
		SystemRuleManager.loadRules(rules);
	}
	
	public synchronized static void initWhiteRule(JSONObject obj) {
		List<AuthorityRule> rules = Lists.newArrayList();
		
		AuthorityRule rule1 = new AuthorityRule();
		rule1.setResource(obj.getString("resource"));
		rule1.setStrategy(RuleConstant.AUTHORITY_WHITE);
		rule1.setLimitApp(obj.getString("limitIps"));
		rules.add(rule1);
		
		AuthorityRule rule2 = new AuthorityRule();
		rule2.setResource(obj.getString("resourceJson"));
		rule2.setStrategy(RuleConstant.AUTHORITY_WHITE);
		rule2.setLimitApp(obj.getString("limitIps"));
		rules.add(rule2);
		
		AuthorityRuleManager.loadRules(rules);
	}
	
	public synchronized static void initblackRule(JSONObject obj) {
		List<AuthorityRule> rules = Lists.newArrayList();
		AuthorityRule rule = new AuthorityRule();
		rule.setResource(obj.getString("resource"));
		rule.setStrategy(RuleConstant.AUTHORITY_BLACK);
		rule.setLimitApp(obj.getString("limitIps"));
		rules.add(rule);
		
		AuthorityRule rule1 = new AuthorityRule();
		rule1.setResource(obj.getString("resourceJson"));
		rule1.setStrategy(RuleConstant.AUTHORITY_BLACK);
		rule1.setLimitApp(obj.getString("limitIps"));
		rules.add(rule1);
		
		AuthorityRuleManager.loadRules(rules);
	}
	
	public synchronized static void initGatewayblackRule(JSONObject obj) {
		List<AuthorityRule> rules = Lists.newArrayList();
		AuthorityRule rule = new AuthorityRule();
		rule.setResource(obj.getString("resource"));
		rule.setStrategy(RuleConstant.AUTHORITY_BLACK);
		String limit = obj.getString("limitIps");
		if(StringUtils.isEmpty(obj.getString("limitHwids"))){
			limit = limit +","+obj.getString("limitHwids");
		}
		rule.setLimitApp(limit);
		rules.add(rule);
		
		AuthorityRule rule1 = new AuthorityRule();
		rule1.setResource(obj.getString("resourceJson"));
		rule1.setStrategy(RuleConstant.AUTHORITY_BLACK);
		rule1.setLimitApp(limit);
		rules.add(rule1);
		
		AuthorityRuleManager.loadRules(rules);
		setOpenGatewayLimit(true);
	}

	public static boolean isOpenGatewayLimit() {
		return openGatewayLimit;
	}

	public static void setOpenGatewayLimit(boolean openGatewayLimit) {
		SentinelManager.openGatewayLimit = openGatewayLimit;
	}
}
