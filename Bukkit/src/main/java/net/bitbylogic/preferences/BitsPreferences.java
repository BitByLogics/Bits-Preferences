package net.bitbylogic.preferences;

import lombok.Getter;
import lombok.NonNull;
import net.bitbylogic.orm.BormAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class BitsPreferences extends JavaPlugin {

    private static final String SQLITE_FILE = "database.sqlite";

    @Getter
    private static volatile BitsPreferences instance;

    @Getter
    private final PreferenceContainer container = new PreferenceContainer();

    private BormAPI bormAPI;
    private boolean disabled;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        DatabaseSettings settings = readDatabaseSettings();

        CompletableFuture.supplyAsync(() -> createBormAPI(settings))
                .thenAccept(this::completeInitialization)
                .exceptionally(e -> {
                    getLogger().severe("Unable to initialize the preference database: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                });

        setupMetrics();
    }

    @Override
    public synchronized void onDisable() {
        disabled = true;
        instance = null;

        if (bormAPI == null) {
            return;
        }

        bormAPI.close();
        bormAPI = null;
    }

    private synchronized void completeInitialization(@NonNull BormAPI bormAPI) {
        if (disabled) {
            bormAPI.close();
            return;
        }

        this.bormAPI = bormAPI;
        container.initialize(bormAPI);
    }

    private void setupMetrics() {
        if (!getConfig().getBoolean("Track-Metrics", true)) {
            return;
        }

        new Metrics(this, 24051);
        getLogger().info("Thank you for allowing metric tracking!");
    }

    private @Nullable DatabaseSettings readDatabaseSettings() {
        ConfigurationSection section = getConfig().getConfigurationSection("Database-Details");

        if (section == null) {
            return null;
        }

        String address = section.getString("Address");
        String database = section.getString("Database");

        if (address == null || address.isEmpty() || database == null || database.isEmpty()) {
            return null;
        }

        return new DatabaseSettings(address, database, section.getString("Port", "3306"),
                section.getString("Username", ""), section.getString("Password", ""));
    }

    private BormAPI createBormAPI(@Nullable DatabaseSettings settings) {
        if (settings == null) {
            getLogger().warning("Database details not provided, using SQLite!");
            return createSQLite();
        }

        registerDriver("com.mysql.cj.jdbc.Driver");

        try {
            return new BormAPI(settings.address(), settings.database(), settings.port(), settings.username(), settings.password());
        } catch (Exception e) {
            getLogger().severe("Error connecting to SQL: " + e.getMessage() + ", falling back to SQLite!");
            e.printStackTrace();
            return createSQLite();
        }
    }

    private BormAPI createSQLite() {
        registerDriver("org.sqlite.JDBC");
        return new BormAPI(new File(SQLITE_FILE));
    }

    private void registerDriver(@NonNull String driverClass) {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            getLogger().severe("JDBC driver " + driverClass + " is not available: " + e.getMessage());
        }
    }

    private record DatabaseSettings(String address, String database, String port, String username, String password) {
    }

}
