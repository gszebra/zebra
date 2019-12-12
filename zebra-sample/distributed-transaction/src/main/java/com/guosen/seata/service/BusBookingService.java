package com.guosen.seata.service;

import com.guosen.seata.model.BusBooking;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

@LocalTCC
public interface BusBookingService {

    @TwoPhaseBusinessAction(name = "BusTccAction" , commitMethod = "commit", rollbackMethod = "rollback")
    void prepare(BusinessActionContext actionContext,
                    @BusinessActionContextParameter(paramName = "busBooking") BusBooking busBooking);

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
