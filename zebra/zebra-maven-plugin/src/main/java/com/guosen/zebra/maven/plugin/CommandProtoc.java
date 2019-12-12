package com.guosen.zebra.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
public class CommandProtoc {

    private final String        discoveryRoot;

    private final File protocDependenciesPath;

    private CommandProtoc(String discoveryRoot, final File protocDependenciesPath){
        this.discoveryRoot = discoveryRoot;
        this.protocDependenciesPath = protocDependenciesPath;
    }

    public static CommandProtoc configProtoPath(String discoveryRoot, final File protocDependenciesPath) {
        return new CommandProtoc(discoveryRoot, protocDependenciesPath);
    }

    public FileDescriptorSet invoke(String protoPath) {
        try {
            Path descriptorPath = Files.createTempFile("descriptor", ".pb.bin");
            ImmutableList.Builder<String> builder = ImmutableList.<String>builder()//
                                                                               .add("--include_std_types")//
                                                                               .add("-I" + discoveryRoot)
                                                                               .add("--descriptor_set_out="  + descriptorPath.toAbsolutePath().toString())//
                                                                               ;
            if (protocDependenciesPath.exists()) {
                File[] files = protocDependenciesPath.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        builder.add("-I" + file.getAbsolutePath());
                    }
                }
            }

            ImmutableList<String> protocArgs = builder.add(protoPath).build();

            int status;
            String[] protocLogLines;
            PrintStream stdoutBackup = System.out;
            try {
                ByteArrayOutputStream protocStdout = new ByteArrayOutputStream();
                System.setOut(new PrintStream(protocStdout));

                status = Protoc.runProtoc(protocArgs.toArray(new String[0]));
                protocLogLines = protocStdout.toString().split("\n");
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to execute protoc binary", e);
            }  catch (InterruptedException e) {
                throw new IllegalArgumentException("Unable to execute protoc binary", e);
            } finally {
                System.setOut(stdoutBackup);
            }
            if (status != 0) {
                System.err.println("Protoc invocation failed with status: " + status);
                for (String line : protocLogLines) {
                	System.err.println("[Protoc log] " + line);
                }

                throw new IllegalArgumentException(String.format("Got exit code [%d] from protoc with args [%s]",
                                                                 status, protocArgs));
            }
            return FileDescriptorSet.parseFrom(Files.readAllBytes(descriptorPath));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
