package pl.skidam.automodpack_core.utils;

public record DownloadSource(String url, Provider provider) {
	public enum Provider {
		SERVER, MODRINTH, CURSEFORGE
	}

	/** The authenticated AutoModpack connection, rather than an external file host. */
	public static DownloadSource server() {
		return new DownloadSource("server://automodpack", Provider.SERVER);
	}
}
