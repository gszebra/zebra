package com.guosen.zebra.console.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.guosen.zebra.console.dto.Result;
import com.guosen.zebra.console.service.ConsoleSerivce;

@RestController
@RequestMapping({ "/api/application" })
public class ApplicationController {
	@Autowired
	private ConsoleSerivce registrySerivce;

	@RequestMapping(value = { "list" }, method = { RequestMethod.GET,
			RequestMethod.POST })
	public Result listAllApps() {
		return Result.builder().withCode(0).withData(this.registrySerivce.getAllApplication()).build();
	}

	@RequestMapping(value = { "all" }, method = { RequestMethod.GET,
			RequestMethod.POST })
	public Result listAll() {
		return Result.builder().withCode(0).withData(new ArrayList<String>(this.registrySerivce.getAllService().keySet()))
				.build();
	}
}
