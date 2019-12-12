package com.guosen.zebra.sample.sharding.dao;

import com.guosen.zebra.sample.sharding.model.Credit;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CreditDao {

    void create(Credit credit);

    void batchCreate(@Param("credits") List<Credit> credits);

    Credit getByDay(@Param("fundId") int fundId, @Param("month") int month, @Param("day") int day);

    List<Credit> getByMonth(@Param("fundId") int fundId, @Param("month") int month);
}
