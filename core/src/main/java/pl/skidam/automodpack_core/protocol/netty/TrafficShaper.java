package pl.skidam.automodpack_core.protocol.netty;

import static pl.skidam.automodpack_core.Constants.LOGGER;
import static pl.skidam.automodpack_core.Constants.serverConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.netty.handler.traffic.GlobalTrafficShapingHandler;

public class TrafficShaper {
	private static final long BYTES_PER_MEGABIT = 1024L * 1024L / 8L;

	private final GlobalTrafficShapingHandler trafficShapingHandler;
	private ScheduledExecutorService executor = null;
	public static TrafficShaper trafficShaper;

	public TrafficShaper(ScheduledExecutorService executor) {
		close(); // There can be only one traffic shaper instance, close previous one if exists
		if (executor == null) {
			executor = Executors.newSingleThreadScheduledExecutor();
			this.executor = executor;
		}

		long bandwidthLimitMbps = configuredBandwidthLimitMbps();
		long bandwidthLimit = bandwidthLimitMbps * BYTES_PER_MEGABIT;
		if (bandwidthLimitMbps < 0) {
			bandwidthLimit = 0;
			LOGGER.warn("Invalid configured bandwidth limit ({} Mbps). Setting effective limit to 0 (unlimited).", bandwidthLimitMbps);
		} else if (bandwidthLimitMbps > 0) {
			LOGGER.info("Setting global server bandwidth limit to {} Mbps.", bandwidthLimitMbps);
		}

		this.trafficShapingHandler = new GlobalTrafficShapingHandler(executor, bandwidthLimit, 0);
		TrafficShaper.trafficShaper = this;
	}

	/**
	 * Returns the configured aggregate server upload limit. The legacy bandwidthLimit remains a
	 * fallback so existing server configurations keep working.
	 */
	static int configuredBandwidthLimitMbps() {
		if (serverConfig == null) return 0;
		if (isNeoForge1211() && serverConfig.globalBandwidthLimit != 0) return serverConfig.globalBandwidthLimit;
		return serverConfig.bandwidthLimit;
	}

	static long configuredBandwidthLimitBytesPerSecond() {
		return Math.max(0L, configuredBandwidthLimitMbps()) * BYTES_PER_MEGABIT;
	}

	static boolean isNeoForge1211() {
		return "1.21.1".equals(pl.skidam.automodpack_core.Constants.MC_VERSION)
				&& "neoforge".equalsIgnoreCase(pl.skidam.automodpack_core.Constants.LOADER);
	}

	public GlobalTrafficShapingHandler getTrafficShapingHandler() {
		return this.trafficShapingHandler;
	}

	public ScheduledExecutorService getExecutor() {
		return this.executor;
	}

	public static void close() {
		if (TrafficShaper.trafficShaper != null) {
			TrafficShaper.trafficShaper.getTrafficShapingHandler().release();
			if (TrafficShaper.trafficShaper.getExecutor() != null) TrafficShaper.trafficShaper.getExecutor().shutdown();
			TrafficShaper.trafficShaper = null;
		}
	}
}
