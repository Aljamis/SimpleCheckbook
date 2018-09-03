package org.avr.simplecheckbook.utils;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * A simple POJO to store the application version info.
 * 
 * Version follows this strategy :  MAJOR.minor-[r|d]build
 * 
 * @author Alfonso
 *
 */
public class CheckBookVersion {
	
	private static final String MAJOR="0";
	private static final String MINOR="0";
	private static String VERSION ;
	private static LocalDate BUILD_DATE;
	private static LocalDate RELEASE_DATE;
	
	/**
	 * static - So this only occurs 1
	 */
	static {
		try {
			Enumeration<URL> resources = CheckBookVersion.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
 			while ( resources.hasMoreElements() ) {
				URL url = resources.nextElement();
				if ( url.toString().contains("SimpleCheckbook.jar") ) {
					VERSION = CheckBookVersion.class.getPackage().getImplementationVersion();
					
					Manifest mf = new Manifest( url.openStream());
					Attributes attr = mf.getMainAttributes();
					if (  attr.getValue("Release-date") == null )
						BUILD_DATE = LocalDate.parse( attr.getValue("Built-date"));
					else 
						RELEASE_DATE = LocalDate.parse( attr.getValue("Release-date"));
					break;
				}
			}
		} catch (IOException e) {
			//  Ignore
		}
	}
	
	
	private CheckBookVersion() { }	/* Make non-instantiate-able */
	
	
	
	public static String getVersion() {
		return MAJOR +"."+ MINOR +"-"+ (RELEASE_DATE == null ? "b" : "r") + VERSION;
	}
	public static String getReleaseDate() {
		if ("yes".equalsIgnoreCase( System.getProperty("run.local") ) )
			return "local : "+ LocalDate.now(); 
		return (RELEASE_DATE == null ? "BUILD : "+BUILD_DATE.toString() : "RELEASE : "+ RELEASE_DATE.toString());
	}
}
