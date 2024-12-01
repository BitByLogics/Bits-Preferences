package net.bitbylogic.preferences;

import lombok.Getter;
import lombok.NonNull;
import net.bitbylogic.apibylogic.database.hikari.HikariAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BitsPreferences extends JavaPlugin {

    @Getter
    private static BitsPreferences instance;

    private HikariAPI hikariAPI;

    @Getter
    private PreferenceContainer container;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        initializeHikari(hikariAPI -> {
            this.hikariAPI = hikariAPI;

            container = new PreferenceContainer(hikariAPI);
        });

        if (!getConfig().getBoolean("Track-Metrics", true)) {
            return;
        }

        new Metrics(this, 24051);
        getLogger().info("Thank you for allowing metric tracking!");
    }

    @Override
    public void onDisable() {
        hikariAPI.getHikari().close();
    }

    private void initializeHikari(@NonNull Consumer<HikariAPI> completeConsumer) {
        if (!getConfig().isSet("Database-Details.Address") || getConfig().getString("Database-Details.Address").isEmpty()
                || !getConfig().isSet("Database-Details.Database") || getConfig().getString("Database-Details.Database").isEmpty()) {
            getLogger().severe("Database details not provided, falling back to SQLite!");
            completeConsumer.accept(new HikariAPI(new File("database.sqlite")));
            return;
        }

        ConfigurationSection hikariSection = getConfig().getConfigurationSection("Database-Details");

        if (hikariSection == null) {
            getLogger().severe("Unable to connect to SQL, falling back to SQLite!");
            completeConsumer.accept(new HikariAPI(new File("database.sqlite")));
            return;
        }

        CompletableFuture.supplyAsync(() -> new HikariAPI(
                        hikariSection.getString("Address"), hikariSection.getString("Database"),
                        hikariSection.getString("Port"), hikariSection.getString("Username"),
                        hikariSection.getString("Password")))
                .thenAccept((hikariAPI) -> {
                    if (hikariAPI == null) {
                        getLogger().severe("Unable to connect to SQL, falling back to SQLite!");
                        completeConsumer.accept(new HikariAPI(new File("database.sqlite")));
                        return;
                    }

                    this.hikariAPI = hikariAPI;

                    completeConsumer.accept(hikariAPI);
                }).exceptionally(e -> {
                    getLogger().severe("Error connecting to SQL: " + e.getMessage() + ", falling back to SQLite!");
                    e.printStackTrace();
                    completeConsumer.accept(new HikariAPI(new File("database.sqlite")));
                    return null;
                });
    }

}
