package com.vip.vjtools.vjkit.net;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import javax.net.ServerSocketFactory;

import org.junit.Test;
import org.mockito.internal.util.io.IOUtil;

public class NetUtilTest {

	@Test
	public void localhost() {
		assertThat(NetUtil.getLocalHost()).isNotEqualTo("127.0.0.1");
		assertThat(NetUtil.getLocalAddress().getHostAddress()).isNotEqualTo("127.0.0.1");
	}

	@Test
	public void portDetect() throws UnknownHostException, IOException {
		int port = NetUtil.findRandomAvailablePort(20000, 20100);
		assertThat(port).isBetween(20000, 20100);
		System.out.println("random port:" + port);

		assertThat(NetUtil.isPortAvailable(port)).isTrue();

		int port2 = NetUtil.findAvailablePortFrom(port);
		assertThat(port2).isEqualTo(port);

		int port3 = NetUtil.findRandomAvailablePort();

		assertThat(port3).isBetween(NetUtil.PORT_RANGE_MIN, NetUtil.PORT_RANGE_MAX);
		System.out.println("random port:" + port3);

		// 尝试占住一个端口
		ServerSocket serverSocket = null;
		try {
			serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 1,
					InetAddress.getByName("localhost"));

			assertThat(NetUtil.isPortAvailable(port)).isFalse();

			int port4 = NetUtil.findAvailablePortFrom(port);
			assertThat(port4).isEqualTo(port + 1);

			try {
				int port5 = NetUtil.findRandomAvailablePort(port, port);
				fail("should fail before");
			} catch (Throwable t) {
				assertThat(t).isInstanceOf(IllegalStateException.class);
			}

		} finally {
			IOUtil.close(serverSocket);
		}

	}

}
