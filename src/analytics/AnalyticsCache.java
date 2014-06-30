package analytics;

import java.util.HashMap;

import com.web.request.HistoricResultSet;

public class AnalyticsCache {
	public static AnalyticsCache instance = new AnalyticsCache();
	HashMap<String, Double> _RSICache;
	
	public AnalyticsCache()
	{
		_RSICache = new HashMap<String,Double>();
	}
	
	public void SetRSI(String Ticker, Double RSI) {
	_RSICache.put(Ticker, RSI);

	}


	public Double GetRSI(String Ticker)
	{
		if(_RSICache.get(Ticker) != null)
			return _RSICache.get(Ticker);
		return 0.0;
	}
}
