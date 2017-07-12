package org.dan.ping.pong.simulate;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class OneGroup4PlayersSim {
    @Test
    @SneakyThrows
    public void simulate() {
        log.info("Press enter to continue...");
        final int c = System.in.read();
        log.info("Read {}", c);
        assertThat(c, greaterThan(0));
    }
}
