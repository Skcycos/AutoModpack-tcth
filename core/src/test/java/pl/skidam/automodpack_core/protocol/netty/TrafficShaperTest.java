package pl.skidam.automodpack_core.protocol.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.skidam.automodpack_core.Constants;
import pl.skidam.automodpack_core.config.Jsons;

class TrafficShaperTest {
	private Jsons.ServerConfigFieldsV3 previousConfig;
	private String previousMinecraftVersion;
	private String previousLoader;

	@BeforeEach
	void setUp() {
		previousConfig = Constants.serverConfig;
		previousMinecraftVersion = Constants.MC_VERSION;
		previousLoader = Constants.LOADER;
		Constants.serverConfig = new Jsons.ServerConfigFieldsV3();
		Constants.MC_VERSION = "1.21.1";
		Constants.LOADER = "neoforge";
	}

	@AfterEach
	void tearDown() {
		TrafficShaper.close();
		Constants.serverConfig = previousConfig;
		Constants.MC_VERSION = previousMinecraftVersion;
		Constants.LOADER = previousLoader;
	}

	@Test
	void globalLimitTakesPrecedenceOnNeoForge1211() {
		Constants.serverConfig.globalBandwidthLimit = 50;
		Constants.serverConfig.bandwidthLimit = 10;

		assertEquals(50, TrafficShaper.configuredBandwidthLimitMbps());
		assertEquals(50L * 1024L * 1024L / 8L, TrafficShaper.configuredBandwidthLimitBytesPerSecond());
	}

	@Test
	void legacyLimitRemainsFallbackOutsideNeoForge1211() {
		Constants.MC_VERSION = "1.21.4";
		Constants.serverConfig.globalBandwidthLimit = 50;
		Constants.serverConfig.bandwidthLimit = 10;

		assertEquals(10, TrafficShaper.configuredBandwidthLimitMbps());
	}
}
