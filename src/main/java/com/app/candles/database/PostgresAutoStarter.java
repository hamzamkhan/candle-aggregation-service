package com.app.candles.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class PostgresAutoStarter {
    private static final Logger log = LoggerFactory.getLogger(PostgresAutoStarter.class);

    private static final Network NETWORK = Network.newNetwork();

    private static volatile PostgreSQLContainer<?> postgres;
    private static volatile FixedHostPortGenericContainer<?> pgAdmin;

    private static final int PGADMIN_HOST_PORT = 5050; // fixed port

    public static void maybeStartAndConfigure(Map<String, Object> overrides) {
        String existingUrl = System.getProperty("spring.datasource.url");
        if (existingUrl == null || existingUrl.isBlank()) existingUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (existingUrl != null && !existingUrl.isBlank()) {
            log.info("Using externally provided spring.datasource.url; not starting Testcontainers Postgres/pgAdmin.");
            return;
        }

        String autostart = System.getProperty("app.postgres.autostart");
        if (autostart == null || autostart.isBlank()) autostart = System.getenv("APP_POSTGRES_AUTOSTART");
        if ("false".equalsIgnoreCase(autostart)) {
            log.info("app.postgres.autostart=false; not starting containers.");
            return;
        }

        String image = System.getProperty("app.postgres.image");
        if (image == null || image.isBlank()) image = System.getenv("APP_POSTGRES_IMAGE");
        if (image == null || image.isBlank()) image = "postgres:16";

        postgres = new PostgreSQLContainer<>(image)
                .withDatabaseName("candles")
                .withUsername("admin")
                .withPassword("admin")
                .withNetwork(NETWORK)
                .withNetworkAliases("candles-db");
        postgres.start();

        overrides.put("spring.datasource.url", postgres.getJdbcUrl());
        overrides.put("spring.datasource.username", postgres.getUsername());
        overrides.put("spring.datasource.password", postgres.getPassword());
        overrides.put("spring.datasource.driver-class-name", "org.postgresql.Driver");

        log.info("Started Postgres container at {}", postgres.getJdbcUrl());
        startPgAdmin();
    }

    private static void startPgAdmin() {
        try {
            Path serversJson = Files.createTempFile("pgadmin-servers", ".json");
            Files.writeString(serversJson, """
            {
              "Servers": {
                "1": {
                  "Name": "Candles DB (Testcontainers)",
                  "Group": "Servers",
                  "Host": "candles-db",
                  "Port": 5432,
                  "MaintenanceDB": "candles",
                  "Username": "admin",
                  "SSLMode": "prefer",
                  "PassFile": "/pgpass"
                }
              }
            }
            """);

            Path pgpass = Files.createTempFile("pgpass", ".txt");
            Files.writeString(pgpass, "candles-db:5432:candles:admin:admin\n");

            pgAdmin = new FixedHostPortGenericContainer<>("dpage/pgadmin4:7.8")
                    .withFixedExposedPort(5050, 80)
                    .withNetwork(NETWORK)
                    .withEnv("PGADMIN_DEFAULT_EMAIL", "admin@admin.com")
                    .withEnv("PGADMIN_DEFAULT_PASSWORD", "admin")
                    .withEnv("PGADMIN_SERVER_JSON_FILE", "/pgadmin4/servers.json")
                    .withEnv("PGADMIN_CONFIG_MASTER_PASSWORD_REQUIRED", "False")
                    .withEnv("PGADMIN_REPLACE_SERVERS_ON_STARTUP", "true")
                    .withCopyFileToContainer(MountableFile.forHostPath(serversJson, 0644), "/pgadmin4/servers.json")
                    .withCopyFileToContainer(MountableFile.forHostPath(pgpass, 0600), "/pgpass");

            pgAdmin.start();

            pgAdmin.execInContainer("sh", "-lc", "chmod 600 /pgpass || true");

            var importOut = pgAdmin.execInContainer(
                    "sh", "-lc",
                    "python /pgadmin4/setup.py --load-servers /pgadmin4/servers.json --user admin@admin.com"
            );
            log.info("pgAdmin server import stdout:\n{}", importOut.getStdout());
            log.info("pgAdmin server import stderr:\n{}", importOut.getStderr());

            log.info("pgAdmin started: http://localhost:{} (login: admin@admin.com / admin)", PGADMIN_HOST_PORT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start pgAdmin container", e);
        }
    }
}