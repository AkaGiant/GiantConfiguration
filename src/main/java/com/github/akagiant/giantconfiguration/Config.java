package com.github.akagiant.giantconfiguration;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Config {

	private final Plugin plugin;
	private final String pluginName;

	@Getter
	private FileConfiguration configurationFile;

	private final Path filePath;

	public Config(Plugin plugin, String configPath) {
		this.plugin = plugin;
		this.pluginName = plugin.getName();
		this.filePath = Path.of(plugin.getDataFolder().getAbsolutePath(), configPath);

		saveDefault(configPath);
	}

	private Config(Plugin plugin, Path path) {
		this.plugin = plugin;
		this.pluginName = plugin.getName();
		this.filePath = path;
		this.configurationFile = YamlConfiguration.loadConfiguration(this.filePath.toFile().getAbsoluteFile());
	}

	private void saveDefault(String configPath) {
		if (Files.exists(filePath)) {
			this.configurationFile = YamlConfiguration.loadConfiguration(this.filePath.toFile().getAbsoluteFile());
			return;
		}

		if (plugin.getResource(configPath) == null) {

			try {
				Files.createDirectories(filePath.getParent());
			} catch (IOException e) {
				log(this.pluginName, e.toString());
			}

			try {
				Files.createFile(filePath);
			} catch (IOException e) {
				log(this.pluginName, e.toString());
			}

			this.configurationFile = YamlConfiguration.loadConfiguration(this.filePath.toFile().getAbsoluteFile());

			try {
				this.configurationFile.save(filePath.toFile());
			} catch (IOException e) {
				log(this.pluginName, e.toString());
			}

		} else {
			plugin.saveResource(configPath, false);
			this.configurationFile = YamlConfiguration.loadConfiguration(this.filePath.toFile().getAbsoluteFile());
		}

	}

	/**
	 * saves and reloads the configuration file.
	 */
	public void save() {
		try {
			this.configurationFile.save(this.filePath.toString());
		} catch (IOException e) {
			log(plugin.getName(), "There was an error whilst saving a file.");
			log(this.pluginName, e.toString());
		}
	}

	public String getFileName() { return this.filePath.getFileName().toString(); }

	/**
	 * reloads the configuration file to update getter values.
	 */
	public void reload() {
		configurationFile = YamlConfiguration.loadConfiguration(this.filePath.toFile());

		InputStream stream = plugin.getResource(String.valueOf(this.filePath.getFileName()));
		if (stream == null) return;

		YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
		configurationFile.setDefaults(defaultConfig);
	}

	/**
	 * finds a String value from a provided configuration file
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link String} object, if one matching the path exists, or null if not
	 */
	public Optional<String> getString(String path) {
		if (isSetAndStringIsValid(path)) {
			logError(path, ConfigErrorPaths.expectedString, null, ConfigErrorPaths.string);
			return Optional.empty();
		}
		String string = this.configurationFile.getString(path);
		if (string == null) return Optional.empty();

		return Optional.of(ChatColor.translateAlternateColorCodes('&', string));
	}

	/**
	 * finds a String value from a provided configuration file
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link String} object, if one matching the path exists, or empty if not
	 */
	public Optional<String> getStringSilent(String path) {
		if (isSetAndStringIsValid(path)) {
			return Optional.empty();
		}

		String string = this.configurationFile.getString(path);
		if (string == null) return Optional.empty();
		return Optional.of(ChatColor.translateAlternateColorCodes('&', string));
	}


	/**
	 * finds a List<String> value from a provided configuration file
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link List <String>} object, if one matching the path exists, or null if not
	 */
	public List<String> getStringList(String path) {
		if (!isSet(path)) {
			logError(path, ConfigErrorPaths.expectedStringList, null, ConfigErrorPaths.strings);
			return new ArrayList<>();
		}
		List<String> stringList = this.configurationFile.getStringList(path);
		if (stringList.isEmpty()) {
			logError(path, ConfigErrorPaths.expectedStringList,null, ConfigErrorPaths.strings);
			return new ArrayList<>();
		}

		List<String> formatted = new ArrayList<>();
		for (String str : this.configurationFile.getStringList(path)) {
			formatted.add(
				ChatColor.translateAlternateColorCodes('&', str));
		}

		return formatted;
	}

	/**
	 * finds a boolean value from a provided configuration file
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Boolean} object, if one matching the path exists, or null if not
	 */
	public boolean getBoolean(String path) {
		if (!isSet(path)) {
			this.configurationFile.set(path, false);
			this.save();
			return false;
		}
		return this.configurationFile.getBoolean(path);
	}

	/**
	 * Checks weather or not a path is valid and something is set.
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Boolean} object, if one matching the path exists, or null if not
	 */
	public boolean isSet(String path) {
		return this.configurationFile.isSet(path);
	}

	/**
	 * Checks weather or not a path is valid and a boolean is set to true.
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Boolean} object, if one matching the path exists, or null if not
	 */
	public boolean isSetAndIsBoolean(String path) {
		return isSet(path) && getBoolean(path);
	}

	/**
	 * Checks weather or not a path is valid and something is set.
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Boolean} object, if one matching the path exists, or null if not
	 */
	public boolean isSetAndStringIsValid(String path) {
		return !this.configurationFile.isSet(path) || this.configurationFile.getString(path) == null;
	}

	public Optional<Material> getMaterial(String path) {
		if (!isSet(path)) return Optional.empty();

		Optional<String> stringOptional = getString(path);
		if (stringOptional.isEmpty()) return Optional.empty();

		String materialString = stringOptional.get();

		if (Material.matchMaterial(materialString) == null) return Optional.empty();
		return Optional.of(Material.valueOf(materialString));
	}


	/**
	 * returns a double value from a given path.
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Double} object, if one matching the path exists, or null if not
	 * @throws NullPointerException if the path is null or value is not present.
	 */
	public Optional<Double> getDouble(String path) {
		if (!isSet(path)) {
			logError(path, ConfigErrorPaths.valueMissing, null, ConfigErrorPaths.decimalNumber);
			return Optional.empty();
		}
		return Optional.of(this.configurationFile.getDouble(path));
	}

	/**
	 * Usage of Wildcard due to FileConfigurations
	 * not being able to return a Double unless it has a .x value.
	 * If a user wants to define 10, they can also define 10.1 in most cases.
	 * Only using an Integer or a Double will not work in this case.
	 */
	public Optional<Number> getNumber(String path) {
		if (!isSet(path)) {
			logError(path, ConfigErrorPaths.valueMissing, null, ConfigErrorPaths.decimalNumber);
			return Optional.empty();
		}

		if (this.configurationFile.isDouble(path)) {
			Optional<Double> value = getDouble(path);
			return value.map(d -> d);
		}
		else if (this.configurationFile.isInt(path)) {
			Optional<Integer> value = getInt(path);
			return value.map(d -> d);
		}
		return Optional.empty();
	}

	public Optional<Number> getNumberNoLog(String path) {
		if (!isSet(path)) {
			return Optional.empty();
		}

		if (this.configurationFile.isDouble(path)) {
			Optional<Double> value = getDouble(path);
			return value.map(d -> d);
		}
		else if (this.configurationFile.isInt(path)) {
			Optional<Integer> value = getInt(path);
			return value.map(d -> d);
		}
		return Optional.empty();
	}

	/**
	 * returns a integer value from a given path.
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Integer} object, if one matching the path exists, or null if not
	 * @throws NullPointerException if the path is null or value is not present.
	 */
	public Optional<Integer> getInt(String path) {
		if (!isSet(path)) {
			logError(path, ConfigErrorPaths.valueMissing, null, ConfigErrorPaths.wholeNumber);
			return Optional.empty();
		}

		if (!this.configurationFile.isInt(path)) {
			logError(path, ConfigErrorPaths.valueNotValid, null, ConfigErrorPaths.wholeNumber);
			return Optional.empty();
		}

		return Optional.of(this.configurationFile.getInt(path));
	}

	public Optional<Integer> getIntSilent(String path) {
		if (!isSet(path)) {
			return Optional.empty();
		}

		if (!this.configurationFile.isInt(path)) {
			return Optional.empty();
		}

		return Optional.of(this.configurationFile.getInt(path));
	}

	public long getLong(String path) {
		if (!isSet(path)) {
			logError(path, ConfigErrorPaths.valueMissing, null, ConfigErrorPaths.wholeNumber);
			return -1;
		}

		if (!this.configurationFile.isLong(path)) {
			logError(path, ConfigErrorPaths.valueNotValid, null, ConfigErrorPaths.wholeNumber);
			return -1;
		}

		return this.configurationFile.getLong(path);
	}

	public float getFloat(String path) {
		double db = this.configurationFile.getDouble(path);
		return (float) db;
	}

	/**
	 * returns a configuration in a configuration file at a given path.
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link ConfigurationSection} object, if one matching the path exists, or null if not
	 * @throws NullPointerException if the path is null or value is not present.
	 */
	public ConfigurationSection getConfigurationSection(String path) {
		if (this.configurationFile.getConfigurationSection(path) == null) return null;
		return this.configurationFile.getConfigurationSection(path);
	}

	public Optional<Sound> getSound(String path) {
		Optional<String> stringOptional = getStringSilent(path);
		Sound sound;

		if (stringOptional.isEmpty()) return Optional.empty();

		try {
			sound = Sound.valueOf(stringOptional.get());
		} catch (IllegalArgumentException e) {
			logError(path, ConfigErrorPaths.expectedSound, "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html");
			return Optional.empty();
		}
		return Optional.of(sound);
	}

	public Optional<Sound> getSoundOptional(String path) {
		String stringOptional = getStringSilent(path).orElse(null);
		if (stringOptional == null) return Optional.empty();
		Sound sound;

		try {
			sound = Sound.valueOf(stringOptional);
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
		return Optional.of(sound);
	}

	private void logError(String path, String errorMessage, @Nullable String url, String... expected) {

		log(this.pluginName, "&m————————————————————————————————————");
		log(this.pluginName, "&fConfiguration Error");
		log(this.pluginName, "&m————————————————————————————————————");
		log(this.pluginName, "&fError: " + errorMessage);
		log(this.pluginName, "&fFile: " + this.getFileName());
		log(this.pluginName, "&fPath: ");
		printPath(path, expected);
		if (url != null) { log(this.pluginName, "&b" + url + " &ffor more information"); }

		log(this.pluginName, "&m————————————————————————————————————");
	}

	private void printPath(String path, String... expected) {

		String[] splitPath = path.split("\\.");
		for (int i = 0; i < splitPath.length; i++) {

			final String whiteSpace = new String(new char[i]).replace("\0", "  ");

			if (i != splitPath.length - 1) {
				log(this.pluginName, "&f" + whiteSpace + splitPath[i] + ":");
				continue;
			}

			log(this.pluginName, "&f" + whiteSpace + splitPath[i] + ": <- Expected " + Arrays.toString(expected));
			if (expected.length <= 1) return;
			for (String str : expected) {
				log(this.pluginName, "&f" + whiteSpace + " - " + str);
			}
		}
	}

	public static Config get(Plugin plugin, String fileName) {
		List<Config> configs = getAll(plugin);
		for (Config config : configs) {
			if (config.getFileName().equals(fileName)) return config;
		}

		return null;
	}

	public static List<Config> getAll(Plugin plugin) {
		List<Path> pathList = listp(Path.of(plugin.getDataFolder().toURI()));

		List<Config> configs = new ArrayList<>();
		for (Path path : pathList) {
			if (Files.isDirectory(path)) continue;
			configs.add(new Config(plugin, path));
		}

		return configs;
	}

	private static List<Path> listp(Path path) {
		List<Path> results = new ArrayList<>();

		try {
			Stream<Path> paths = Files.list(path);
			paths.forEach(path2 -> {
				if (Files.isDirectory(path2)) {
					results.addAll(listp(path2));
				} else {
					results.add(path2);
				}
			});
			paths.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return results;
	}

	private static void log(String pluginName, String message) {
		Bukkit.getConsoleSender().sendMessage(
			ChatColor.translateAlternateColorCodes('&', "&8[&c" + pluginName + " &c&lSEVERE&8] &f" + message)
		);
	}
}
