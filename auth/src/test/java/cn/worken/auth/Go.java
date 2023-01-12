package cn.worken.auth;

import java.util.Random;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

/**
 * @author shaoyijiong
 * @date 2020/7/1
 */
public class Go {

    @Test
    public void a() {
        System.out.println(RandomStringUtils.randomAscii(10));
        System.out.println(RandomStringUtils.randomAlphabetic(10));
        System.out.println(RandomStringUtils.randomGraph(10));
        System.out.println(RandomStringUtils.random(10));
    }
}
