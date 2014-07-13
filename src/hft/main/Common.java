package hft.main;

import org.apache.log4j.Logger;

public class Common {
	private  Logger log = Logger.getLogger( this.getClass() );
	public static Common instance = new Common();
	
	/**
	 * @param args
	 */
	public void WriteToLog(String message) {
		log.info(message);

	}

}
