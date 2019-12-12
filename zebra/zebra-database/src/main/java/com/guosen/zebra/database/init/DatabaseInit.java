/**   
* @Title: DatabaseInit.java 
* @Package com.guosen.zebra.database.init 
* @author 邓启翔
* @date 2018年1月24日 上午10:28:17 
* @version V1.0   
*/
package com.guosen.zebra.database.init;

import com.guosen.zebra.database.exception.ZebraBeansException;
import com.guosen.zebra.database.mybatis.conf.DataSourceCfg;
import com.guosen.zebra.database.mybatis.conf.DataSourceCfgUtil;
import com.guosen.zebra.database.sharding.conf.*;
import com.guosen.zebra.database.utils.OperateProperties;
import com.guosen.zebra.database.valiate.DataSourceValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName: DatabaseInit
 * @author 邓启翔
 * @date 2018年1月24日 上午10:28:17
 */
@Component("databaseInit")
@Order(1)
@DependsOn("applicationContextUtil")
public class DatabaseInit implements BeanDefinitionRegistryPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInit.class);

	/**
	 * Zebra配置文件名称
	 */
	private static final String ZEBRA_PROPERTIES_FILE_NAME = "localCache";

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// do nothing
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
		createRegisterDbRelatedBdf(registry);
	}

	private void createRegisterDbRelatedBdf(BeanDefinitionRegistry registry) {

		// 加载localCache.properties
		manuallyLoadProperties();

		List<DataSourceCfg> dataSourceCfgList = DataSourceCfgUtil.getDataSourceCfgList();
		if (CollectionUtils.isEmpty(dataSourceCfgList)) {

			// 该微服务未配置任何数据源，直接返回。
			LOGGER.info("There is no data source configuration.");
			return;
		}

		ZebraShardingConfiguration zebraShardingConfiguration =
				ZebraShardingConfigurationUtil.getZebraShardingConfiguration();
		Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap
				= zebraShardingConfiguration == null ? Collections.emptyMap() : zebraShardingConfiguration.getShardingcfg();

		List<DataSourceCfg> standaloneDataSourceCfgList = getStandaloneDataSourceCfgs(dataSourceCfgList,
				zebraShardingDataSourceConfigurationMap);
		List<DataSourceCfg> commonDataSourceCfgList = getCommonDataSourceCfgs(dataSourceCfgList,
				zebraShardingDataSourceConfigurationMap);

		// 校验
		DataSourceValidator.validate(standaloneDataSourceCfgList, commonDataSourceCfgList, zebraShardingDataSourceConfigurationMap);

		// 注册单独的数据源（也就是没有被sharding数据源引用的数据源）
		StandaloneDbInit standaloneDbInit = new StandaloneDbInit();
		standaloneDbInit.init(registry, standaloneDataSourceCfgList);

		// 注册被sharding引用但同时也有自己basePackage的数据源
		CommonDbInit commonDbInit = new CommonDbInit();
		commonDbInit.init(registry, commonDataSourceCfgList);

		// 注册sharding数据源相关的BeanDefinition
		ShardingDbInit shardingDbInit = new ShardingDbInit();
		shardingDbInit.init(registry, dataSourceCfgList, zebraShardingDataSourceConfigurationMap);
	}

	private void manuallyLoadProperties() {
		try {
			OperateProperties.loadProperties(ZEBRA_PROPERTIES_FILE_NAME);
		}
		catch (IOException e) {
			String errorMessage = "Failed to load properties file, file name : " + ZEBRA_PROPERTIES_FILE_NAME;
			LOGGER.error(errorMessage);
			throw new ZebraBeansException(errorMessage, e);
		}
	}

	private List<DataSourceCfg> getStandaloneDataSourceCfgs(List<DataSourceCfg> dataSourceCfgList,
															Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {
		Set<String> dsOfSharing = getReferDataSourceOfSharding(zebraShardingDataSourceConfigurationMap);

		return dataSourceCfgList.stream()
				.filter((dataSourceCfg) -> !dsOfSharing.contains(dataSourceCfg.getDataSourceName()))
				.collect(Collectors.toList());
	}

	private List<DataSourceCfg> getCommonDataSourceCfgs(List<DataSourceCfg> dataSourceCfgList,
															Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {
		Set<String> dsOfSharing = getReferDataSourceOfSharding(zebraShardingDataSourceConfigurationMap);

		// 查找出被sharding引用，并且有自己的basePackage的数据源
		return dataSourceCfgList.stream()
				.filter((dataSourceCfg) -> dsOfSharing.contains(dataSourceCfg.getDataSourceName()))
				.filter((dataSourceCfg) -> StringUtils.isNotBlank(dataSourceCfg.getBasePackage()))
				.collect(Collectors.toList());
	}

	private Set<String> getReferDataSourceOfSharding(
			Map<String, ZebraShardingDataSourceCfg> zebraShardingDataSourceConfigurationMap) {
		Set<String> referDataSourceNames = new HashSet<>();
		for (ZebraShardingDataSourceCfg zebraShardingDataSourceCfg : zebraShardingDataSourceConfigurationMap.values()) {
			addReferDataSources(referDataSourceNames, zebraShardingDataSourceCfg);
		}

		return referDataSourceNames;
	}

	private void addReferDataSources(Set<String> referDataSourceNames, ZebraShardingDataSourceCfg zebraShardingDataSourceCfg) {
		ZebraReferDataSource zebraReferDataSource = zebraShardingDataSourceCfg.getDatasource();
		if (zebraReferDataSource != null) {
			Set<String> shardingReferDsName = zebraReferDataSource.getNames();
			if (CollectionUtils.isNotEmpty(shardingReferDsName)) {
				referDataSourceNames.addAll(shardingReferDsName);
			}
		}

		ZebraMasterSlaveRuleConfiguration zebraMasterSlaveRuleConfiguration = zebraShardingDataSourceCfg.getMasterslave();
		if (zebraMasterSlaveRuleConfiguration != null) {
			String masterDsName = zebraMasterSlaveRuleConfiguration.getMasterDataSourceName();
			Collection<String> slaveDsNames = zebraMasterSlaveRuleConfiguration.getSlaveDataSourceNames();

			if (StringUtils.isNotBlank(masterDsName)) {
				referDataSourceNames.add(masterDsName);
			}
			if (CollectionUtils.isNotEmpty(slaveDsNames)) {
				referDataSourceNames.addAll(slaveDsNames);
			}
		}
	}
}
