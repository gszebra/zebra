package com.guosen.zebra.proto2java.plugin;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.collect.Lists;
import com.guosen.zebra.maven.plugin.CommonProto2Java;

@Mojo(name = "proto2java", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class Proto2Java extends AbstractMojo{

    @Parameter(defaultValue = "src/main/proto")
    private String     protoPath;

    @Parameter(defaultValue = "src/main/java")
    private String     buildPath;
    @Parameter(
        required = true,
        defaultValue = "${project.build.directory}/protoc-dependencies"
    )
    private File protocDependenciesPath;

    private List<File> allProtoFile = Lists.newArrayList();
    
//    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File deirectory = new File(protoPath);
        listAllProtoFile(deirectory);
        CommonProto2Java protp2ServicePojo = CommonProto2Java.forConfig(protoPath, buildPath, protocDependenciesPath);
        for (File file : allProtoFile) {
            if (file.exists()) {
                String protoFilePath = file.getPath();
                protp2ServicePojo.generateFile(protoFilePath);
            }
        }
    }

    private File listAllProtoFile(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                File[] fileArray = file.listFiles();
                if (fileArray != null) {
                    for (int i = 0; i < fileArray.length; i++) {
                        listAllProtoFile(fileArray[i]);
                    }
                }
            } else {
                if (StringUtils.endsWith(file.getName(), "proto")) {
                    allProtoFile.add(file);
                }
            }
        }
        return null;
    }
}
