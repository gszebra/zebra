package com.guosen.zebra.cache.common;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RocketMqGroupNameNormalizierTest {

    @Test
    public void test1() {
        String originalGroupName = "com.guosen.zebra.hello1.service";

        String normalizedGroupName = RocketMqGroupNameNormalizier.normalize(originalGroupName);

        assertThat(normalizedGroupName, is("com-guosen-zebra-hello1-service"));
    }

    @Test
    public void test2() {
        String originalGroupName = "com.$GUOSEN.z%ebra.hello1.service";

        String normalizedGroupName = RocketMqGroupNameNormalizier.normalize(originalGroupName);

        assertThat(normalizedGroupName, is("com--GUOSEN-z%ebra-hello1-service"));
    }
}
