package com.github.akagiant.giantconfiguration;

import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class GiantConfiguration extends JavaPlugin implements Listener {

	@Getter
	private static Plugin plugin;

	Config config;
	Config deepConfig;

	@Override
	public void onEnable() {
		plugin = this;

		getServer().getPluginManager().registerEvents(this, this);

		config = new Config(this, "config.yml");
		deepConfig = new Config(this, "deepFolder/deepConfig.yml");

		Config dynamicConfig = new Config(this, "deepFolder2/deepConfig2.yml");
		dynamicConfig.getConfigurationFile().set("test", "lol");
		dynamicConfig.save();

	}

	int i = 0;

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {

		config.getConfigurationFile().set("test2", config.getString("test2") + " " + i);
		config.save();

		deepConfig.getConfigurationFile().set("test", deepConfig.getString("test") + " " + i);
		deepConfig.save();
		i++;

		config.reload();
		deepConfig.save();
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		e.getPlayer().sendMessage(config.getString("test2").orElse("poo"));
		e.getPlayer().sendMessage(deepConfig.getString("test").orElse("poo2"));
	}


}
