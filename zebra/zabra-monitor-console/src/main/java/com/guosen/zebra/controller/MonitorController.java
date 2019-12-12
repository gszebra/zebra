package com.guosen.zebra.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.guosen.zebra.admin.vo.MetricItem;
import com.guosen.zebra.service.MonitorService;

//@EnableConfigurationProperties(ZebraConfProp.class)
@Controller
public class MonitorController {

    @Resource
    MonitorService mSvc;

    @RequestMapping(value ="/metrics")
    @ResponseBody
    public ResponseEntity<String> metrics() {
        String ret = mSvc.fetchAllNodeMetrics();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/plain; version=0.0.4");
        return ResponseEntity.ok().headers(headers).body(new String(ret));
    }

    @RequestMapping("/health")
    @ResponseBody
    public List<MetricItem> health() {
        return mSvc.getCollectedMetrics();
    }

}
