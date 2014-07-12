package analytics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class AnalyticsTests {

	/**
	 * @param args
	 */
	public void FileWriter(String line) {
	
		 try {
			 Writer output = new BufferedWriter(new FileWriter("c:\\Users\\Ben\\AAPL2.csv", true));
			 
			 output.append(line+";");
			 output.close();
				
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
