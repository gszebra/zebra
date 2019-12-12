/**   
* @Title: HealthImpl.java 
* @Package com.guosen.zebra.core.monitor.health 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年11月23日 下午4:43:41 
* @version V1.0   
*/
package com.guosen.zebra.core.monitor.health;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.AbstractApplicationContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.guosen.zebra.core.exception.RpcFrameworkException;
import com.guosen.zebra.core.grpc.anotation.ZebraService;
import com.guosen.zebra.core.grpc.util.SslUtil;
import com.guosen.zebra.core.monitor.health.ServiceParam.HealthRequest;
import com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse;
import com.guosen.zebra.core.monitor.health.ServiceParam.HealthResponse.Builder;
import com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse;
import com.guosen.zebra.core.monitor.health.ServiceParam.ServingStatus;
import com.guosen.zebra.core.serializer.utils.ReflectUtils;
import com.guosen.zebra.core.util.ApplicationContextUtil;
import com.guosen.zebra.monitor.metrics.exporter.CompactPrometheusTextFormat;

import io.grpc.Channel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * @ClassName: HealthImpl
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 邓启翔
 * @date 2017年11月23日 下午4:43:41
 * 
 */
public class Health extends HealthGrpc.HealthImplBase {
	private static final Logger log = LogManager.getLogger(Health.class);

	private AbstractApplicationContext applicationContext;
	
	private final String flowRule="1";
	private final String degradeRule="2";
	private final String systemRule="3";
	private final String whiteRule="4";
	private final String blackRule="5";
	private final String apiBlackRule="6";

	public Health(AbstractApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@SuppressWarnings("deprecation")
	public Channel getChannel(String ip, int port) {
		Channel channel = NettyChannelBuilder.forAddress(ip, port)
				// .overrideAuthority(ip)
				// .sslContext(buildClientSslContext())//
				// .negotiationType(NegotiationType.TLS)
				.usePlaintext(true)//
				.build();
		return channel;
	}

	@SuppressWarnings("unused")
	private SslContext buildClientSslContext() {
		try {
			return GrpcSslContexts
					.configure(SslContextBuilder.forClient()//
							.keyManager(SslUtil.loadFileCert("server.pem"), SslUtil.loadFileCert("server_pkcs8.key"))
							.trustManager(SslUtil.loadX509Cert("server.pem")))//
					.build();
		} catch (Exception e) {
			throw new RpcFrameworkException(e);
		}
	}

	public void check(HealthRequest request, io.grpc.stub.StreamObserver<HealthResponse> responseObserver) {
		String service = request.getService();
		try {
			Object obj = applicationContext.getBeansOfType(ReflectUtils.name2class(service));
			if (obj != null) {
				HealthResponse response = HealthResponse.newBuilder().setStatus(ServingStatus.SERVING).build();
				responseObserver.onNext(response);
				responseObserver.onCompleted();
			} else {
				HealthResponse response = HealthResponse.newBuilder().setStatus(ServingStatus.NOT_SERVING).build();
				responseObserver.onNext(response);
				responseObserver.onCompleted();
			}
		} catch (BeansException | ClassNotFoundException e) {
			HealthResponse response = HealthResponse.newBuilder().setStatus(ServingStatus.UNKNOWN).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
	}
	@SuppressWarnings("resource")
	public void getMethods(HealthRequest request, io.grpc.stub.StreamObserver<HealthResponse> responseObserver) {
		String service = request.getService();
		try {
			List<Object> services = ApplicationContextUtil.getTypedBeansWithAnnotation(service, ZebraService.class);
			Class<?> clz = services.get(0).getClass();
			String path = clz.getProtectionDomain().getCodeSource().getLocation().getPath();
			JarFile jarFile = new JarFile(path);
			Enumeration<JarEntry> enu = jarFile.entries();
			List<String> urls = Lists.newArrayList();
			while (enu.hasMoreElements()) {
				JarEntry element = (JarEntry) enu.nextElement();
				String name = element.getName();
				if (name.endsWith(".proto")) {
					urls.add(name);
				}
			}
			StringBuffer protoBuff = new StringBuffer();
			for (String url : urls) {
				InputStream in = clz.getClassLoader().getResourceAsStream(url);
				if (in != null) {
					protoBuff.append(inputStream2String(in));
					in.close();
					protoBuff.append("\n");
				}
			}
			Method[] mds = clz.getMethods();
			Builder builder = HealthResponse.newBuilder();
			for (Method md : mds) {
				log.debug("{} methods :{}", clz.getName(), md.getName());
				builder.addMethodName(md.getName());
			}
			builder.setMetrics(protoBuff.toString());

			responseObserver.onNext(builder.build());
			responseObserver.onCompleted();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			HealthResponse response = HealthResponse.newBuilder().build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
	}

	public String inputStream2String(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	public void getMetrics(HealthRequest request, io.grpc.stub.StreamObserver<HealthResponse> responseObserver) {
		final String metricsStr = CompactPrometheusTextFormat.getMetricsString();
		HealthResponse response = HealthResponse.newBuilder().setMetrics(metricsStr).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	public void setSentinel(com.guosen.zebra.core.monitor.health.ServiceParam.SentinelRequest request,
			io.grpc.stub.StreamObserver<com.guosen.zebra.core.monitor.health.ServiceParam.SentinelResponse> responseObserver) {
		String type = request.getType();
		try{
			JSONArray array = JSONArray.parseArray(request.getData());
			System.err.println(array);
			for(Object obj: array){
				if(flowRule.equals(type)){
					SentinelManager.initFlowQpsRule((JSONObject) obj);
				}else if(degradeRule.equals(type)){
					SentinelManager.initDegradeRule((JSONObject) obj);
				}else if(systemRule.equals(type)){
					SentinelManager.initSystemProtectionRule((JSONObject) obj);
				}else if(whiteRule.equals(type)){
					SentinelManager.initWhiteRule((JSONObject) obj);
				}else if(blackRule.equals(type)){
					SentinelManager.initblackRule((JSONObject) obj);
				}else if(apiBlackRule.equals(type)){
					SentinelManager.initGatewayblackRule((JSONObject) obj);
				}
				
			}
			SentinelResponse response = SentinelResponse.newBuilder().setCode(1).setMsg("设置成功").build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			SentinelResponse response = SentinelResponse.newBuilder().setCode(0).setMsg("设置失败").build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
	}
}
