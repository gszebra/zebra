package com.guosen.zebra.generate;

import java.io.IOException;
import java.util.Scanner;

import com.guosen.zebra.generate.utils.Generate;

public class MainClass
{

	public MainClass()
	{
	}

	@SuppressWarnings("static-access")
	public static void main(String args[]) throws InterruptedException
	{
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		System.out.println("是否是生成API接口(Y/N)：");
		String type = in.next();
		System.out.println("请输入要生成的项目名称：");
		String name = in.next();
		System.out.println("输入groupId：");
		String artifactId = in.next();
		System.out.println("项目生成中...");
		Generate g = new Generate();
		try
		{
			String s = g.create(name,type,artifactId);
			System.out.println("生成成功，新生成的项目在以下路径:");
			System.out.println(s);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		Thread.currentThread().sleep(5000);
	}
}
