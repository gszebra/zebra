package com.guosen.zebra.console.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {
	@Autowired
	private ConsoleInterceptor interceptor;

	@Bean
	public ViewResolver getViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/");
		resolver.setSuffix(".html");
		return resolver;
	}

	@Bean
	public HttpMessageConverters fastJsonConfigure() {
		FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		fastJsonConfig.setSerializerFeatures(new SerializerFeature[] { SerializerFeature.PrettyFormat });

		fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
		converter.setFastJsonConfig(fastJsonConfig);
		return new HttpMessageConverters(new HttpMessageConverter[] { converter });
	}

	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(new String[] { "/**" }).addResourceLocations(new String[] { "classpath:/static/" });
		super.addResourceHandlers(registry);
	}

	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this.interceptor).addPathPatterns(new String[] { "/**" }).excludePathPatterns(
				new String[] { "/api/user/login", "/login.html", "/frame/**", "/js/**", "/page/**" });
		super.addInterceptors(registry);
	}
}
