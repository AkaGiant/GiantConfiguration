package com.github.akagiant.giantconfiguration;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Config {

	private final Plugin plugin;


	@Getter
	private FileConfiguration configurationFile;

	private final Path filePath;

	public Config(Plugin plugin, String configPath) {
		this.plugin = plugin;

		this.filePath = Path.of(plugin.getDataFolder().getAbsolutePath(), configPath);

		plugin.saveResource(configPath, false);
		this.configurationFile = YamlConfiguration.loadConfiguration(this.filePath.toFile().getAbsoluteFile());
	}

	public void save() {
		try {
			this.getConfigurationFile().save(this.filePath.toString());
		} catch (IOException e) {
			ConfigUtil.log(plugin.getName(), "There was an error whilst saving a file.");
			throw new RuntimeException(e);
		}
	}

	public String getFileName() { return this.filePath.getFileName().toString(); }
	public String getPluginName() { return this.plugin.getName(); }

	public void reload() {
		configurationFile = YamlConfiguration.loadConfiguration(this.filePath.toFile());

		InputStream stream = plugin.getResource(String.valueOf(this.filePath.getFileName()));
		if (stream == null) return;

		YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
		configurationFile.setDefaults(defaultConfig);
	}

	public Config get(String fileName) {
		Path path = Path.of(this.plugin.getDataFolder().toURI());
		List<Config> configs = getAll(path);
		for (Config config : configs) {
			if (config.getFileName().equals(fileName)) return config;
		}

		return null;
	}

	public List<Config> getAll(Path path) {
		List<Config> configs = new ArrayList<>();

		try {
			Stream<Path> files = Files.list(path);
			files.forEach(file -> configs.add(new Config(this.plugin, file.getFileName().toString())));
			files.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return configs;
	}

	public Config create(Path path) {
		try {
			Files.createFile(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		FileConfiguration configuration = YamlConfiguration.loadConfiguration(path.toFile());
		try {
			configuration.save(path.toFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return new Config(this.plugin, path.toFile().getName());
	}
}
