// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   ZipUtil.java

package com.guosen.zebra.generate.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil
{

	public ZipUtil()
	{
	}

	public void zip(String zipFileName, File inputFile)
		throws Exception
	{
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		zip(out, inputFile, "");
		out.flush();
		out.close();
	}

	private void zip(ZipOutputStream out, File f, String base)
		throws Exception
	{
		if (f.isDirectory())
		{
			File fl[] = f.listFiles();
			if (base != "")
				out.putNextEntry(new ZipEntry((new StringBuilder()).append(base).append("/").toString()));
			base = base.length() != 0 ? (new StringBuilder()).append(base).append("/").toString() : "";
			for (int i = 0; i < fl.length; i++)
				zip(out, fl[i], (new StringBuilder()).append(base).append(fl[i].getName()).toString());

		} else
		{
			out.putNextEntry(new ZipEntry(base));
			FileInputStream in = new FileInputStream(f);
			int b;
			while ((b = in.read()) != -1) 
				out.write(b);
			in.close();
		}
	}
}
