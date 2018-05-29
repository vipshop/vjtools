package com.vip.vjtools.vjkit.concurrent;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.vip.vjtools.vjkit.concurrent.jsr166e.LongAdder;

public class ConcurrentsTest {

	@Test
	public void longAdder() {
		LongAdder counter = Concurrents.longAdder();
		counter.increment();
		counter.add(2);
		assertThat(counter.longValue()).isEqualTo(3L);
	}

}
