package net.bitbylogic.preferences;

import lombok.Getter;
import lombok.NonNull;
import net.bitbylogic.orm.BormAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BitsPreferences extends JavaPlugin {

    @Getter
    private static BitsPreferences instance;

    private BormAPI bormAPI;

    @Getter
    private PreferenceContainer container;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        initializeBorm(bormAPI -> {
            this.bormAPI = bormAPI;

            container = new PreferenceContainer(bormAPI);
        });

        if (!getConfig().getBoolean("Track-Metrics", true)) {
            return;
        }

        new Metrics(this, 24051);
        getLogger().info("Thank you for allowing metric tracking!");
    }

    @Override
    public void onDisable() {
        bormAPI.close();
    }

    private void initializeBorm(@NonNull Consumer<BormAPI> completeConsumer) {
        if (!getConfig().isSet("Database-Details.Address") || getConfig().getString("Database-Details.Address").isEmpty()
                || !getConfig().isSet("Database-Details.Database") || getConfig().getString("Database-Details.Database").isEmpty()) {
            getLogger().severe("Database details not provided, falling back to SQLite!");
            completeConsumer.accept(new BormAPI(new File("database.sqlite")));
            return;
        }

        ConfigurationSection databaseSections = getConfig().getConfigurationSection("Database-Details");

        if (databaseSections == null) {
            getLogger().severe("Unable to connect to SQL, falling back to SQLite!");
            completeConsumer.accept(new BormAPI(new File("database.sqlite")));
            return;
        }

        CompletableFuture.supplyAsync(() -> new BormAPI(
                        databaseSections.getString("Address"), databaseSections.getString("Database"),
                        databaseSections.getString("Port"), databaseSections.getString("Username"),
                        databaseSections.getString("Password")))
                .thenAccept((bormAPI) -> {
                    if (bormAPI == null) {
                        getLogger().severe("Unable to connect to SQL, falling back to SQLite!");
                        completeConsumer.accept(new BormAPI(new File("database.sqlite")));
                        return;
                    }

                    this.bormAPI = bormAPI;

                    completeConsumer.accept(bormAPI);
                }).exceptionally(e -> {
                    getLogger().severe("Error connecting to SQL: " + e.getMessage() + ", falling back to SQLite!");
                    e.printStackTrace();
                    completeConsumer.accept(new BormAPI(new File("database.sqlite")));
                    return null;
                });
    }

}
