/**   
* @Title: MyBatisConfig.java 
* @Package com.yy.bg.mybatis.conf 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年5月18日 下午4:27:47 
* @version V1.0   
*/
package com.guosen.zebra.conf.mybatis.conf;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.guosen.zebra.conf.properties.MybatisProp;

/** 
* @ClassName: MyBatisConfig 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2017年5月18日 下午4:27:47 
*  
*/
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(MybatisProp.class)
public class MyBatisConfig implements TransactionManagementConfigurer {
    @Autowired
    DataSource dataSource;
    @Autowired
    MybatisProp prop;
    
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean() {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setTypeAliasesPackage(prop.getTypeAliasesPackage());
        //添加XML目录
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            bean.setMapperLocations(resolver.getResources(prop.getMapperLocations()));
            return bean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 用于实际查询的sql工具,传统dao开发形式可以使用这个,基于mapper代理则不需要注入
     * @param sqlSessionFactory
     * @return
     */
    @Bean(name = "sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    /**
     * 事务管理,具体使用在service层加入@Transactional注解
     */
    @Bean(name = "transactionManager")
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}