package com.guosen.zebra.generate.utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.apache.commons.io.FileUtils;


public class Generate {

	private String tplPath;
	public String outPath;
	private Map<String,String> map;
	private String groupId;

	public Generate() {
		File f = new File("");
		tplPath = (new StringBuilder()).append(f.getAbsolutePath()).append(File.separator).append("tpl").toString();
		outPath = (new StringBuilder()).append(f.getAbsolutePath()).append(File.separator).append("out").append(File.separator).toString();
	}

	public String create(String name,String type,String groupId) throws IOException {
		this.groupId = groupId;
		map = new HashMap<String, String>();
		if(type.toUpperCase().equals("Y")){
			map.put("src", "zebra-api");
			map.put("dest", name +"-api");
		}else{
			map.put("src", "zebra-server");
			map.put("dest", name);
		}
		
		createProjectDir(map.get("src"),map.get("dest"));
		System.out.println("outPath："+outPath);
		replaceContent(new File(outPath+File.separator+map.get("dest")));
		return outPath;
	}

	private void replaceContent(File fileDir) throws IOException {
		File arr[] = fileDir.listFiles();
		int len = arr.length;
		for (int i = 0; i< len; i++) {
			File file = arr[i];
			if (file.isFile()) {
				List<String> list = FileUtils.readLines(file, "UTF-8");
				File tempFile = new File((new StringBuilder()).append(outPath).append("temp")
						.append(UUID.randomUUID().toString()).toString());
				for (Iterator<String> it = list.iterator(); it.hasNext(); FileUtils.writeStringToFile(tempFile, "\r\n", true)) {
					String str = (String) it.next();
					if(file.getName().equals("pom.xml")){
						if(str.contains("<groupId>com.example</groupId>")){
							str =str.replaceAll("com.example", groupId);
						}else if(str.contains("<artifactId>zebra-api</artifactId>")){
							str =str.replace("zebra-api", map.get("dest"));
						}else if(str.contains("<artifactId>example-server</artifactId>")){
							str =str.replace("example-server", map.get("dest"));
						}else if(str.contains("<finalName>demo</finalName>")){
							str =str.replace("demo", map.get("dest"));
						}
					}
					if(file.getName().equals("log4j2.xml")){
						if(str.contains("example")){
							str =str.replace("example", map.get("dest"));
						}
					}
					FileUtils.writeStringToFile(tempFile, repalceName(str), "UTF-8", true);
				}
				FileUtils.copyFile(tempFile, file);
				tempFile.delete();
			} else {
				replaceContent(file);
			}
		}

	}

	private void createProjectDir(String from,String to) throws IOException {
		FileUtils.copyDirectory(new File(tplPath+File.separator+from), new File(outPath+File.separator+to));
		File tempfile = new File(outPath+File.separator+to);
		System.out.println("project name :" + tempfile.getName());
		rePeojectName(tempfile);
	}

	private void rePeojectName(File dirFile) {
		if (dirFile.isDirectory()) {
			File fs[] = dirFile.listFiles();
			File arr$[] = fs;
			int len$ = arr$.length;
			for (int i$ = 0; i$ < len$; i$++) {
				File file = arr$[i$];
				String destName = repalceName(file.getName());
				File project = new File((new StringBuilder()).append(file.getParent()).append(File.separator)
						.append(destName).toString());
				file.renameTo(project);
				rePeojectName(project);
			}

		}
	}

	private String repalceName(String fileName) {
		return fileName.replace((CharSequence) map.get("src"), (CharSequence) map.get("dest"));
	}
}
