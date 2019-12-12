package com.guosen.seata.service;

import com.guosen.seata.model.HotelBooking;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

@LocalTCC
public interface HotelBookingService {

    /**
     * 添加TwoPhaseBusinessAction注解
     */
    @TwoPhaseBusinessAction(name = "HotelTccAction" , commitMethod = "commit", rollbackMethod = "rollback")
    void prepare(BusinessActionContext actionContext,
                    @BusinessActionContextParameter(paramName = "hotelBooking") HotelBooking hotelBooking);

    /**
     * Commit boolean.
     *
     * @param actionContext the action context
     * @return the boolean
     */
    boolean commit(BusinessActionContext actionContext);

    /**
     * Rollback boolean.
     *
     * @param actionContext the action context
     * @return the boolean
     */
    boolean rollback(BusinessActionContext actionContext);
}
