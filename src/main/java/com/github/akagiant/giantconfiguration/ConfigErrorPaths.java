package com.github.akagiant.giantconfiguration;

class ConfigErrorPaths {

	private ConfigErrorPaths() {
		//no instance
	}
	
	static String string = "String ('your text here')";
	static String[] strings = new String[]{"'line 1'", "'line 2'", "'line 3'"};
	static String wholeNumber = "Whole Number E.G. 1, 2, 3";
	static String decimalNumber = "Decimal or Whole Number E.G. 1, 1.0, 4.23";
	static String valueMissing = "The value expected at the current path is missing.";
	static String valueNotValid = "The value inputted at the current path is not valid.";
	static String expectedStringList = "List of String Not Found";
	static String expectedString = "String Not Found";
	static String expectedSound = "Sound Not Found";

}
