/**   
* @Title: SpringMVCConfig.java 
* @Package com.guosen.zebra.conf 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2018年4月20日 下午3:46:04 
* @version V1.0   
*/
package com.guosen.zebra.conf;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.common.collect.Lists;

/** 
* @ClassName: SpringMVCConfig 
* @Description: TODO(这里用一句话描述这个类的作用) 
* @author 邓启翔 
* @date 2018年4月20日 下午3:46:04 
*  
*/
@Configuration
public class SpringMVCConfig extends WebMvcConfigurationSupport{
	// 自定义消息转化器的第二种方法
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter converter  = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        converters.add(converter);
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteMapNullValue);

        List<MediaType> fastMediaTypes = Lists.newArrayListWithExpectedSize(2);
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastMediaTypes.add(MediaType.TEXT_PLAIN);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        converters.add(fastConverter);
    }
}
