package com.guosen.seata.service.impl;


import com.guosen.seata.model.BusBooking;
import com.guosen.seata.model.HotelBooking;
import com.guosen.seata.service.BookingService;
import com.guosen.seata.service.BusBookingService;
import com.guosen.seata.service.HotelBookingService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingServiceImpl implements BookingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingServiceImpl.class);
    
    @Autowired
    private BusBookingService busBookingService;

    @Autowired
    private HotelBookingService hotelBookingService;

    /**
     * 添加GlobalTransactional注解，以便开启分布式事务
     * 必须在实现类上面加，不能在接口上加。
     */
    @GlobalTransactional
    @Override
    public void booking() {
        LOGGER.info("Begin booking");

        String xid = RootContext.getXID();
        LOGGER.info("xid is : {}", xid);

        BusBooking busBooking = new BusBooking();
        busBooking.setBusId("bus001");
        busBooking.setUserId("user001");

        HotelBooking hotelBooking = new HotelBooking();
        hotelBooking.setHotelId("hotel001");
        hotelBooking.setUserId("user001");

        // 调用TCC各个服务的prepare方法
        // TCC在prepare执行完毕后，如果全部prepare都成功，则调用对应的commit函数(一直尝试直到所有commit成功)
        // 如果有部分prepare失败，则调用对应的rollback函数（一直尝试直到所有rollback成功）

        BusinessActionContext actionContext = new BusinessActionContext();
        busBookingService.prepare(actionContext, busBooking);
        hotelBookingService.prepare(actionContext, hotelBooking);

        LOGGER.info("Finish booking");
    }

}
