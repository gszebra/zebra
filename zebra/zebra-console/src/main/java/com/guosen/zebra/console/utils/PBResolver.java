package com.guosen.zebra.console.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PBResolver {
	public static JSONObject getPbResolver(String proto) {
		proto = proto.replaceAll("}syntax", "}\r\nsyntax");
		List<String> protos = Arrays.asList(proto.split("\r\nsyntax"));
		JSONArray msgs = new JSONArray();
		JSONArray mds = new JSONArray();
		protos.forEach((pb) -> {
			Pattern r = Pattern.compile("package\\s+([a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*);");
			Matcher m = r.matcher(pb);
			String pbPkg = "";
			if (m.find()) {
				pbPkg = m.group(1);
			}

			pb = pb.replaceAll("[syntax]*[\\d\\D]+\"proto3\";", "");
			pb = pb.replaceAll(
					"option\\s+java_package\\s*=\\s*\"([a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*)\";\r\n", "");
			pb = pb.replaceAll("option\\s+java_outer_classname\\s*=\\s*\"\\w+\";\r\n", "");
			pb = pb.replaceAll("package\\s+([a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*);\r\n", "");
			pb = pb.replaceAll("import\\s+\"\\S+\"\\s*;\r\n", "");
			JSONArray array = getPbMessage(pb, pbPkg);
			if (array != null) {
				msgs.addAll(array);
			}

			array = getPbService(pb);
			if (array != null) {
				mds.addAll(array);
			}

		});
		JSONObject result = new JSONObject();
		result.put("msgs", msgs);
		result.put("mds", mds);
		return result;
	}

	public static JSONArray getPbMessage(String pb, String pkgName) {
		Pattern r = Pattern.compile("([//]*.*[\r\n]*\\s*message\\s+\\w+\\s*\\{[^[{[\\d\\D]}]]*\\})");
		Matcher m = r.matcher(pb);
		if (m.find()) {
			String msg = m.group();
			Map<String, Object> map = getMessages(msg);
			List<JSONObject> List = tranferMap2List(pkgName, map);
			JSONArray array = new JSONArray();
			array.addAll(List);
			return array;
		} else {
			return null;
		}
	}

	public static JSONArray getPbService(String pb) {
		Pattern r = Pattern.compile("([//]*.*[\r\n]*\\s*service\\s+\\w+\\s*\\{[^[{[\\d\\D]}]]*\\})");
		Matcher m = r.matcher(pb);
		if (m.find()) {
			String msg = m.group();
			return getServiceDefine(msg);
		} else {
			return null;
		}
	}

	private static Map<String, Object> getMessages(String msg) {
		StringBuffer ctx = new StringBuffer();
		int i = 0;
		Map<String, Object> inMap = Maps.newHashMap();
		int inNum = 0;

		while (i < msg.length() && msg.charAt(i) != 0) {
			String context;
			String keyName;
			if ("}".charAt(0) == msg.charAt(i)) {
				inMap.put("idx", i + 1);
				context = ctx.toString().trim();
				keyName = "message";
				if (context.contains("oneof")) {
					keyName = "oneof";
				} else if (!context.startsWith("message")) {
					keyName = "field";
				} else {
					context = context.replaceAll("//.*[\r\n]+", "");
					context = context.substring(context.indexOf("message") + 7).trim();
				}

				if (inMap.get("message" + inNum) == null && !StringUtils.isEmpty(context)) {
					inMap.put(keyName + inNum, context);
				}

				return inMap;
			}

			if ("{".charAt(0) != msg.charAt(i) && ";".charAt(0) != msg.charAt(i)) {
				ctx.append(msg.charAt(i));
			} else {
				if ("{".charAt(0) == msg.charAt(i)) {
					context = ctx.toString().trim();
					keyName = "message";
					if (context.contains("oneof")) {
						keyName = "oneof";
					} else if (!context.startsWith("message")) {
						keyName = "field";
					} else {
						context = context.replaceAll("//.*[\r\n]+", "");
						context = context.substring(context.indexOf("message") + 7).trim();
					}

					inMap.put(keyName + inNum, context);
					ctx = new StringBuffer();
					++i;
					Map<String, Object> in = getMessages(msg.substring(i, msg.length()));
					inMap.put("in" + inNum, in);
					++inNum;
					i += (Integer) in.get("idx");
					continue;
				}

				if (";".charAt(0) == msg.charAt(i)) {
					context = ctx.toString().trim();
					if (context != null && !"".equals(context)) {
						inMap.put("field" + inNum, context);
						ctx = new StringBuffer();
						++inNum;
					}
				}
			}

			++i;
		}

		return inMap;
	}
	@SuppressWarnings("unchecked")
	private static List<JSONObject> tranferMap2List(String packgeName, Map<String, Object> param) {
		List<JSONObject> list = Lists.newArrayList();

		for (int i = 0; param.get("message" + i) != null; ++i) {
			JSONObject map = new JSONObject();
			String msgName = (String) param.get("message" + i);
			map.put("name", packgeName + "." + msgName);
			
			Map<String, Object> body = (Map<String, Object>) param.get("in" + i);
			int j = 0;

			for (int idx = 0; body.get("field" + j) != null || body.get("oneof" + j) != null; ++j) {
				String field = (String) body.get("field" + j);
				Map<String,String> fieldMap = Maps.newConcurrentMap();
				if (field == null) {
					field = (String) body.get("oneof" + j);
					field = field.substring(0, field.indexOf("oneof"));
					fieldMap =  (Map<String, String>) body.get("in" + j);
					field = field + " oneof " + fieldMap.get("field0");
				}

				if (field.indexOf("=") > 0) {
					field = field.substring(0, field.indexOf("="));
				}

				field = field.replaceAll(";", "");
				field = field.trim();
				fieldMap = getField(packgeName, field);
				if (!fieldMap.isEmpty()) {
					map.put("field" + idx, fieldMap);
					++idx;
				}
			}

			list.add(map);
		}

		return list;
	}

	private static Map<String, String> getField(String packgeName, String field) {
		Map<String, String> map = Maps.newHashMap();
		Pattern r = Pattern.compile("([//]+.*([\r\n]+.*[\r\n]+))+");
		Matcher m = r.matcher(field);

		while (m.find()) {
			map.put("commont", m.group());
		}

		r = Pattern.compile("(\\w+\\s+)*(\\w+)\\s+(\\w+)\\s*");
		m = r.matcher(field);
		ArrayList<String> list = Lists.newArrayList();

		while (m.find()) {
			list.add(m.group(1));
			list.add(m.group(2));
			list.add(m.group(3));
		}

		if (list.size() == 3) {
			map.put("option", list.get(0) == null ? "" : ((String) list.get(0)).trim().replaceAll("\r\n", ""));
			map.put("type", ((String) list.get(1)).trim().replaceAll("\r\n", ""));
			map.put("name", ((String) list.get(2)).trim().replaceAll("\r\n", ""));
		} else {
			r = Pattern.compile("(\\w+\\s+)*([a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*)\\s+(\\w+)\\s*");
			m = r.matcher(field);

			while (m.find()) {
				map.put("option", m.group(1));
				map.put("type", m.group(2));
				map.put("name", m.group(4));
			}
		}

		checkRef(packgeName, map);
		return map;
	}

	private static void checkRef(String packgeName, Map<String, String> map) {
		if (!map.isEmpty()) {
			List<String> list = Arrays
					.asList("bool,double,float,int32,uin32,int64,uint64,sint32,sing64,fixed32,fixed64,sfixed32,sfixed64,string,bytes,enum,message"
							.split(","));
			if (!list.contains(map.get("type"))) {
				if (map.get("type") == null || !((String) map.get("type")).contains("map")) {
					if (((String) map.get("type")).split("\\.").length > 1) {
						map.put("ref", map.get("type"));
					} else {
						map.put("ref", packgeName + "." + (String) map.get("type"));
					}

				}
			}
		}
	}

	public static JSONArray getServiceDefine(String msg) {
		JSONArray array = new JSONArray();
		msg = msg.replaceAll("[\r\n]*\\s*service\\s+\\w+\\s*\\{", "");
		msg = msg.substring(0, msg.lastIndexOf("}") - 1);
		String[] mds = msg.split("}");
		String[] var3 = mds;
		int var4 = mds.length;

		for (int var5 = 0; var5 < var4; ++var5) {
			String md = var3[var5];
			md = md.replace("{", "");
			md = md.trim();
			JSONObject json = new JSONObject();
			Pattern r = Pattern.compile("([//]+.*([\r\n]+.*[\r\n]+))+");

			Matcher m;
			for (m = r.matcher(md); m.find(); md = md.replaceAll(m.group(), "")) {
				json.put("commont", m.group());
			}

			md = md.trim();
			r = Pattern.compile(
					"rpc\\s+(\\w+)\\s+\\((([a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*))\\)\\s+returns\\s+\\((([a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*))\\)");
			m = r.matcher(md);

			while (m.find()) {
				json.put("name", m.group(1));
				json.put("req", m.group(2));
				json.put("rsp", m.group(5));
			}

			array.add(json);
		}

		return array;
	}

	public static void main(String[] args) {
		// String pb = "message ArtsMonitorResp {\r\n repeated
		// com.guosen.zebra.dto.ResultDTO result = 1;\r\n repeated
		// ArtsDBMonitorDTO db = 2;\r\n repeated ArtsCJGMonitorDTO cjg = 3;\r\n
		// repeated ArtsCoinMonitorDTO coin = 4;\r\n}";
		// JSONArray array = getPbMessage(pb, "com.guosen.arts.auth.model");
		// System.err.println(array.toJSONString());

		String a = "// message \r\n sasss";
		System.err.println(a.replaceAll("//.*[\r\n]+", ""));
	}
}