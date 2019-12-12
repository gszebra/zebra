package com.guosen.zebra.geshell.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "genShell", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenShell extends AbstractMojo{

    @Parameter(defaultValue = "zebra-app")
    private String     jarName;

    @Parameter(defaultValue = "zebra-app")
    private String     appName;
    
    @Parameter
    private String     filePath;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
    	System.out.println("[INFO] zebra maven plugin out jarName:" +jarName +".jar");
    	System.out.println("[INFO] zebra maven plugin out appName:" +appName);
    	System.out.println("[INFO] zebra maven plugin out filePath:" +filePath+File.separator+"target"+File.separator+jarName+".zip");
    	try{
    		overrideSh(filePath+File.separator+"src"+File.separator+"main"+File.separator+"bin"+File.separator+"tmpStart.sh");
    		overrideSh(filePath+File.separator+"src"+File.separator+"main"+File.separator+"bin"+File.separator+"tmpStop.sh");
    		overrideSh(filePath+File.separator+"src"+File.separator+"main"+File.separator+"bin"+File.separator+"tmpCheck.sh");
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    
    private void overrideSh(String filename){
    	BufferedReader br =null;
    	BufferedWriter bw = null;
    	try{
    		br = new BufferedReader(new FileReader(filename));
            String line = "";  
            line = br.readLine();  
            StringBuffer buf = new StringBuffer();
            buf.append("#!/bin/bash").append("\n");
            while (line != null) {  
                line = br.readLine();
                if(line ==null) continue;
                line = Parser.parse0("jar", line, jarName+".jar");
                line = Parser.parse0("app", line, appName);
                buf.append(line);
                buf.append("\n");
            }  
            if(filename.contains("tmpStart.sh")){
            	bw = new BufferedWriter(new FileWriter(filePath+File.separator+"src"+File.separator+"main"+File.separator+"bin"+File.separator+"start.sh"));
            }else if(filename.contains("tmpStop.sh")){
            	bw = new BufferedWriter(new FileWriter(filePath+File.separator+"src"+File.separator+"main"+File.separator+"bin"+File.separator+"stop.sh"));
            }else{
            	bw = new BufferedWriter(new FileWriter(filePath+File.separator+"src"+File.separator+"main"+File.separator+"bin"+File.separator+"check.sh"));
            }
            bw.write(buf.toString());
    	}catch(Exception e){
    		e.printStackTrace();
    	}finally {
    		if (br != null) {
    			try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    		if (bw != null) {
    			try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
		}
    	
    }
}
