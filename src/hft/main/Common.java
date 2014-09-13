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
	 void PrintStartup() {
		 String[] Diamond =
			   {"      /\\    ",
			    "     /  \\   ",
			    "    /    \\  ",
			    "    \\    /  ",
			    "     \\  /   ",
			    "      \\/    "};
		 for (int i = 0; i < Diamond.length; ++i) 
			{
				log.info(Diamond[i]);
			}
		 log.info("Startup....Free memory : "+Runtime.getRuntime().freeMemory());
		 log.info("Initialising HFT module... ");
	}
}
