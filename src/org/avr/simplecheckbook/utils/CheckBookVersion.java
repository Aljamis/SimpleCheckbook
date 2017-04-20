package org.avr.simplecheckbook.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Properties;

/**
 * A simple POJO to store the application version info
 * @author Alfonso
 *
 */
public class CheckBookVersion {
	
	private static String versionNum = "Not Yet Set";
	private static LocalDate releaseDate;
	
	/**
	 * static - So this only occurs 1
	 */
	static {
		try {
			Properties props = new Properties();
			FileInputStream in = new FileInputStream("CB.version");
			props.load(in);
			
			versionNum = props.getProperty("num");
			releaseDate = LocalDate.parse( props.getProperty("date") );
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}
	
	
	public static String getVersion() {
		StringBuffer str = new StringBuffer();
		str.append("Simple CheckBook.  Released:  ").append( releaseDate );
		str.append("         Version: " ).append( versionNum );
		return str.toString();
	}
	
	public static String getVersionOnly() {
		return versionNum;
	}
	public static String getReleaseDate() {
		return releaseDate.toString();
	}
}
