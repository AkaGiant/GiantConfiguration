package com.github.akagiant.giantconfiguration;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConfigUtil {

	private final String pluginName;

	public ConfigUtil(String pluginName) {
		this.pluginName = pluginName;
	}

	/**
	 * finds a String value from a provided configuration file
	 *
	 * @param config {@link Config} the config to get the data from
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link String} object, if one matching the path exists, or null if not
	 */
	public Optional<String> getString(Config config, String path) {
		if (isSetAndStringIsValid(config, path)) {
			logError(config, path, ConfigErrorPaths.expectedString, null, ConfigErrorPaths.string);
			return Optional.empty();
		}
		String string = config.getConfigurationFile().getString(path);
		if (string == null) return Optional.empty();

		return Optional.of(ChatColor.translateAlternateColorCodes('&', string));
	}

	public Optional<String> getStringSilent(Config config, String path) {
		if (isSetAndStringIsValid(config, path)) {
			return Optional.empty();
		}

		String string = config.getConfigurationFile().getString(path);
		if (string == null) return Optional.empty();
		return Optional.of(ChatColor.translateAlternateColorCodes('&', string));
	}


	/**
	 * finds a List<String> value from a provided configuration file
	 *
	 * @param config {@link Config} the config to get the data from
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link List <String>} object, if one matching the path exists, or null if not
	 * @throws NullPointerException if the path is null or value is not present.
	 */
	public List<String> getStringList(Config config, String path) throws NullPointerException {
		if (!isSet(config, path)) {
			logError(config, path, ConfigErrorPaths.expectedStringList, null, ConfigErrorPaths.strings);
			return new ArrayList<>();
		}
		List<String> stringList = config.getConfigurationFile().getStringList(path);
		if (stringList.isEmpty()) {
			logError(config, path, ConfigErrorPaths.expectedStringList,null, ConfigErrorPaths.strings);
			return new ArrayList<>();
		}

		List<String> formatted = new ArrayList<>();
		for (String str : config.getConfigurationFile().getStringList(path)) {
			formatted.add(
				ChatColor.translateAlternateColorCodes('&', str));
		}

		return formatted;
	}

	/**
	 * finds a boolean value from a provided configuration file
	 *
	 * @param config {@link Config} the config to get the data from
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Boolean} object, if one matching the path exists, or null if not
	 */
	public boolean getBoolean(Config config, String path) {
		if (!isSet(config, path)) {
			config.getConfigurationFile().set(path, false);
			config.save();
			return false;
		}
		return config.getConfigurationFile().getBoolean(path);
	}

	/**
	 * Checks weather or not a path is valid and something is set.
	 *
	 * @param config {@link Config} the config to get the data from
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Boolean} object, if one matching the path exists, or null if not
	 */
	public boolean isSet(Config config, String path) {
		return config.getConfigurationFile().isSet(path);
	}

	/**
	 * Checks weather or not a path is valid and a boolean is set to true.
	 *
	 * @param config {@link Config} the config to get the data from
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Boolean} object, if one matching the path exists, or null if not
	 */
	public boolean isSetAndIsBoolean(Config config, String path) {
		return isSet(config, path) && getBoolean(config, path);
	}

	/**
	 * Checks weather or not a path is valid and something is set.
	 *
	 * @param config {@link Config} the config to get the data from
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Boolean} object, if one matching the path exists, or null if not
	 */
	public boolean isSetAndStringIsValid(Config config, String path) {
		return !config.getConfigurationFile().isSet(path) || config.getConfigurationFile().getString(path) == null;
	}

	public Optional<Material> getMaterial(Config config, String path) throws NullPointerException {
		if (!isSet(config, path)) return Optional.empty();

		Optional<String> stringOptional = getString(config, path);
		if (stringOptional.isEmpty()) return Optional.empty();

		String materialString = stringOptional.get();

		if (Material.matchMaterial(materialString) == null) return Optional.empty();
		return Optional.of(Material.valueOf(materialString));
	}


	/**
	 * returns a double value from a given path.
	 *
	 * @param config {@link Config} the config to get the data from
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Double} object, if one matching the path exists, or null if not
	 * @throws NullPointerException if the path is null or value is not present.
	 */
	public Optional<Double> getDouble(Config config, String path) throws NullPointerException  {
		if (!isSet(config, path)) {
			logError(config, path, ConfigErrorPaths.valueMissing, null, ConfigErrorPaths.decimalNumber);
			return Optional.empty();
		}
		return Optional.of(config.getConfigurationFile().getDouble(path));
	}

	/**
	 * Usage of Wildcard due to FileConfigurations
	 * not being able to return a Double unless it has a .x value.
	 * If a user wants to define 10, they can also define 10.1 in most cases.
	 * Only using an Integer or a Double will not work in this case.
	 */
	public Optional<Number> getNumber(Config config, String path) {
		if (!isSet(config, path)) {
			logError(config, path, ConfigErrorPaths.valueMissing, null, ConfigErrorPaths.decimalNumber);
			return Optional.empty();
		}

		if (config.getConfigurationFile().isDouble(path)) {
			Optional<Double> value = getDouble(config, path);
			return value.map(d -> d);
		}
		else if (config.getConfigurationFile().isInt(path)) {
			Optional<Integer> value = getInt(config, path);
			return value.map(d -> d);
		}
		return Optional.empty();
	}

	public Optional<Number> getNumberNoLog(Config config, String path) {
		if (!isSet(config, path)) {
			return Optional.empty();
		}

		if (config.getConfigurationFile().isDouble(path)) {
			Optional<Double> value = getDouble(config, path);
			return value.map(d -> d);
		}
		else if (config.getConfigurationFile().isInt(path)) {
			Optional<Integer> value = getInt(config, path);
			return value.map(d -> d);
		}
		return Optional.empty();
	}

	/**
	 * returns a integer value from a given path.
	 *
	 * @param config {@link Config} the config to get the data from
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link Integer} object, if one matching the path exists, or null if not
	 * @throws NullPointerException if the path is null or value is not present.
	 */
	public Optional<Integer> getInt(Config config, String path) throws NullPointerException {
		if (!isSet(config, path)) {
			logError(config, path, ConfigErrorPaths.valueMissing, null, ConfigErrorPaths.wholeNumber);
			return Optional.empty();
		}

		if (!config.getConfigurationFile().isInt(path)) {
			logError(config, path, ConfigErrorPaths.valueNotValid, null, ConfigErrorPaths.wholeNumber);
			return Optional.empty();
		}

		return Optional.of(config.getConfigurationFile().getInt(path));
	}

	public Optional<Integer> getIntSilent(Config config, String path) throws NullPointerException {
		if (!isSet(config, path)) {
			return Optional.empty();
		}

		if (!config.getConfigurationFile().isInt(path)) {
			return Optional.empty();
		}

		return Optional.of(config.getConfigurationFile().getInt(path));
	}

	public long getLong(Config config, String path) throws NullPointerException {
		if (!isSet(config, path)) {
			logError(config, path, ConfigErrorPaths.valueMissing, null, ConfigErrorPaths.wholeNumber);
			return -1;
		}

		if (!config.getConfigurationFile().isLong(path)) {
			logError(config, path, ConfigErrorPaths.valueNotValid, null, ConfigErrorPaths.wholeNumber);
			return -1;
		}

		return config.getConfigurationFile().getLong(path);
	}

	public float getFloat(Config config, String path) {
		double db = config.getConfigurationFile().getDouble(path);
		return (float) db;
	}

	/**
	 * returns a configuration in a configuration file at a given path.
	 *
	 * @param config {@link Config} the config to get the data from
	 * @param path {@link String} the path to the data within the configuration file.
	 *
	 * @return a {@link ConfigurationSection} object, if one matching the path exists, or null if not
	 * @throws NullPointerException if the path is null or value is not present.
	 */
	public ConfigurationSection getConfigurationFileurationSection(Config config, String path) throws NullPointerException  {
		if (config.getConfigurationFile().getConfigurationSection(path) == null) return null;
		return config.getConfigurationFile().getConfigurationSection(path);
	}

	public Optional<Sound> getSound(Config config, String path) {
		Optional<String> stringOptional = getStringSilent(config, path);
		Sound sound;

		if (stringOptional.isEmpty()) return Optional.empty();

		try {
			sound = Sound.valueOf(stringOptional.get());
		} catch (IllegalArgumentException e) {
			logError(config, path, ConfigErrorPaths.expectedSound, "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html");
			return Optional.empty();
		}
		return Optional.of(sound);
	}

	public Optional<Sound> getSoundOptional(Config config, String path) {
		String stringOptional = getStringSilent(config, path).orElse(null);
		if (stringOptional == null) return Optional.empty();
		Sound sound;

		try {
			sound = Sound.valueOf(stringOptional);
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
		return Optional.of(sound);
	}

	private void logError(Config config, String path, String errorMessage, @Nullable String url, String... expected) {

		log(this.pluginName, "&m————————————————————————————————————");
		log(this.pluginName, "&fConfiguration Error");
		log(this.pluginName, "&m————————————————————————————————————");
		log(this.pluginName, "&fError: " + errorMessage);
		log(this.pluginName, "&fFile: " + config.getFileName());
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

	public static void log(String pluginName, String message) {
		Bukkit.getConsoleSender().sendMessage(
			ChatColor.translateAlternateColorCodes('&', "&8[&c" + pluginName + " &c&lSEVERE&8] &f" + message)
		);
	}

}

