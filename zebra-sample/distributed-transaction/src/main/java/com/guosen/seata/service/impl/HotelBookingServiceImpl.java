package com.guosen.seata.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guosen.seata.model.HotelBooking;
import com.guosen.seata.service.HotelBookingService;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HotelBookingServiceImpl implements HotelBookingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotelBookingServiceImpl.class);

    @Override
    public void prepare(BusinessActionContext actionContext, HotelBooking hotelBooking) {

        long branchId = actionContext.getBranchId();
        LOGGER.info("Hotel booking prepare branch id : {}", branchId);

        LOGGER.info("Preparing booking hotel {}", JSON.toJSONString(hotelBooking));

        // 请在这里做prepare阶段的资源锁定
    }

    @Override
    public boolean commit(BusinessActionContext actionContext) {
        long branchId = actionContext.getBranchId();
        LOGGER.info("Hotel booking commit branch id : {}", branchId);

        JSONObject hotelBookingJson = (JSONObject)actionContext.getActionContext("hotelBooking");
        if (hotelBookingJson == null) {
            LOGGER.error("in commit hotel booking is null");
        }
        else {
            LOGGER.info("Now commit hotel booking {}", hotelBookingJson);
        }

        // 提交锁定的资源

        return true;
    }

    @Override
    public boolean rollback(BusinessActionContext actionContext) {

        long branchId = actionContext.getBranchId();
        LOGGER.info("Hotel booking rollback branch id : {}", branchId);

        JSONObject hotelBookingJson = (JSONObject)actionContext.getActionContext("hotelBooking");
        if (hotelBookingJson == null) {
            LOGGER.error("In rollback hotel booking is null");
        }
        else {
            LOGGER.info("Now rollback hotel booking {}", hotelBookingJson);
        }

        // 回滚锁定的资源

        return true;
    }
}
