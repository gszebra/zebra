package com.guosen.seata.controller;

import com.guosen.seata.service.BookingService;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 纯粹为了测试简单，使用Rest接口
 */
@RestController
public class BookingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @RequestMapping("/booking/commit")
    public String bookingCommit() {

        LOGGER.info("Begin booking.");

        bookingService.booking();

        LOGGER.info("Finish booking.");

        return "success";
    }

}
