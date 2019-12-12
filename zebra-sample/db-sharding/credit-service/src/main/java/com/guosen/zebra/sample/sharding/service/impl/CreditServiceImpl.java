package com.guosen.zebra.sample.sharding.service.impl;

import com.guosen.zebra.core.grpc.anotation.ZebraService;
import com.guosen.zebra.sample.sharding.credit.dto.creditdtoproto3.*;
import com.guosen.zebra.sample.sharding.credit.service.CreditService;
import com.guosen.zebra.sample.sharding.dao.CreditDao;
import com.guosen.zebra.sample.sharding.model.Credit;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ZebraService
@Component
public class CreditServiceImpl implements CreditService {

    @Autowired
    private CreditDao creditDao;

    @Override
    public Result create(CreditDto creditDto) {

        // 做些动作，然后转换为model，这里什么也不做

        Credit credit = toCredit(creditDto);

        creditDao.create(credit);

        Result result = new Result();
        result.setMessage("success");

        return result;
    }

    private Credit toCredit(CreditDto creditDto) {
        Credit credit = new Credit();
        credit.setId(creditDto.getId());
        credit.setFundId(creditDto.getFundId());
        credit.setMonth(creditDto.getMonth());
        credit.setDay(creditDto.getDay());
        credit.setOtherField1(creditDto.getOtherField1());
        credit.setOtherField2(creditDto.getOtherField2());
        return credit;
    }

    @Override
    public Result batchCreate(CreditDtos CreditDtos) {

        List<Credit> credits = CreditDtos.getCreditDtos()
                .stream()
                .map(this::toCredit)
                .collect(Collectors.toList());

        creditDao.batchCreate(credits);

        Result result = new Result();
        result.setMessage("success");

        return result;
    }

    @Override
    public QueryResult querySpecificDayCredit(CreditQueryOfDay creditQueryOfDay) {
        Integer fundId = creditQueryOfDay.getFundId();
        Integer day = creditQueryOfDay.getDay();
        int month = day / 100;

        // 查询时，必须传递分库分表条件，month为分表条件，需要计算出来

        Credit credit = creditDao.getByDay(fundId, month, day);
        ArrayList<CreditDto> creditDtos = new ArrayList<>();

        if (credit != null) {
            CreditDto creditDto = toCreditDto(credit);
            creditDtos.add(creditDto);
        }

        QueryResult queryResult = new QueryResult();
        queryResult.setMessage("success");
        queryResult.setCreditDtos(creditDtos);

        return queryResult;
    }


    @Override
    public QueryResult queryMonthCredit(CreditQueryOfMonth creditQueryOfMonth) {
        Integer fundId = creditQueryOfMonth.getFundId();
        Integer month = creditQueryOfMonth.getMonth();

        List<Credit> credits = creditDao.getByMonth(fundId, month);
        ArrayList<CreditDto> creditDtos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(credits)) {
            for (Credit credit : credits) {
                CreditDto creditDto = toCreditDto(credit);
                creditDtos.add(creditDto);
            }
        }

        QueryResult queryResult = new QueryResult();
        queryResult.setMessage("success");
        queryResult.setCreditDtos(creditDtos);

        return queryResult;
    }

    private CreditDto toCreditDto(Credit credit) {
        CreditDto creditDto = new CreditDto();
        creditDto.setId(credit.getId());
        creditDto.setFundId(credit.getFundId());
        creditDto.setMonth(credit.getMonth());
        creditDto.setDay(credit.getDay());
        creditDto.setOtherField1(credit.getOtherField1());
        creditDto.setOtherField2(credit.getOtherField2());
        return creditDto;
    }
}
