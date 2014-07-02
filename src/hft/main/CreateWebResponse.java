package hft.main;

import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import analytics.AnalyticsCache;
import analytics.HistoricalRsiCache;

public class CreateWebResponse {

	/**
	 * @param args
	 */
	public String GetWeb(){
		
		 AnalyticsCache _AnalyticsCache = new AnalyticsCache().instance;
		
		 JSONObject mainObj = new JSONObject();
		 
		 for (Entry<String, HistoricalRsiCache> entry : _AnalyticsCache.GetHistoricalRSIMap().entrySet()) {
			    String key = entry.getKey();
			    HistoricalRsiCache value = (HistoricalRsiCache)entry.getValue();
			   
			    for (Entry<Long, Double> entryHist : value.GetHistMap().entrySet()) {
			    	  long time = entryHist.getKey();
			    	  double rsi = entryHist.getValue();
			    	  JSONObject jo = new JSONObject();
				 jo.put("Time", time);
				 jo.put("RSI", r);

				 JSONArray ja = new JSONArray();
				 ja.put(jo);

				
				 mainObj.put(key, ja);
			    
			}
		
		
		
	}

}
