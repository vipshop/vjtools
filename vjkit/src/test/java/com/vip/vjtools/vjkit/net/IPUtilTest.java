package com.vip.vjtools.vjkit.net;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class IPUtilTest {

	@Test
	public void stringAndInt() {

		assertThat(IPUtil.ipv4StringToInt("192.168.0.1")).isEqualTo(-1062731775);
		assertThat(IPUtil.ipv4StringToInt("192.168.0.2")).isEqualTo(-1062731774);

		assertThat(IPUtil.intToIpv4String(-1062731775)).isEqualTo("192.168.0.1");
		assertThat(IPUtil.intToIpv4String(-1062731774)).isEqualTo("192.168.0.2");
	}

	@Test
	public void inetAddress() {

		assertThat(IPUtil.fromInt(-1062731775).getHostAddress()).isEqualTo("192.168.0.1");
		assertThat(IPUtil.fromInt(-1062731774).getHostAddress()).isEqualTo("192.168.0.2");

		assertThat(IPUtil.fromIpString("192.168.0.1").getHostAddress()).isEqualTo("192.168.0.1");
		assertThat(IPUtil.fromIpString("192.168.0.2").getHostAddress()).isEqualTo("192.168.0.2");
		assertThat(IPUtil.fromIpv4String("192.168.0.1").getHostAddress()).isEqualTo("192.168.0.1");
		assertThat(IPUtil.fromIpv4String("192.168.0.2").getHostAddress()).isEqualTo("192.168.0.2");

		assertThat(IPUtil.toInt(IPUtil.fromIpString("192.168.0.1"))).isEqualTo(-1062731775);
	}
}
