package com.guosen.seata.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guosen.seata.model.BusBooking;
import com.guosen.seata.service.BusBookingService;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BusBookingServiceImpl implements BusBookingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusBookingServiceImpl.class);

    @Override
    public void prepare(BusinessActionContext actionContext, BusBooking busBooking) {
        long branchId = actionContext.getBranchId();
        LOGGER.info("Bus booking prepare branch id : {}", branchId);
        LOGGER.info("Preparing booking bus {}", JSON.toJSONString(busBooking));

        // 请在这里做prepare阶段的资源锁定
    }

    @Override
    public boolean commit(BusinessActionContext actionContext) {

        // 确保commit的幂等性
        long branchId = actionContext.getBranchId();
        LOGGER.info("Bus booking commit branch id : {}", branchId);

        // 在这里可以用actionContext获取到prepare阶段的参数
        JSONObject busBooking = (JSONObject)actionContext.getActionContext("busBooking");
        if (busBooking == null) {
            LOGGER.error("in commit busBooking is null");
        }
        else {
            LOGGER.info("Now commit booking bus {}", busBooking);
        }

        // 提交锁定的资源

        return true;
    }

    @Override
    public boolean rollback(BusinessActionContext actionContext) {

        // 确保rollback的幂等性
        long branchId = actionContext.getBranchId();
        LOGGER.info("Bus booking rollback branch id : {}", branchId);

        JSONObject busBooking = (JSONObject)actionContext.getActionContext("busBooking");
        if (busBooking == null) {
            LOGGER.error("in rollback busBooking is null");
        }
        else {
            LOGGER.info("Now rollback booking bus {}", busBooking);
        }

        // 回滚锁定的资源

        return true;
    }
}
