package zorg.avr.derby;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggingTest {
	
	private static Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		logger.debug("Just debugging");
		logger.info("Just info");
		logger.warn("Just warning");
		logger.error("Just error");
	}

}
