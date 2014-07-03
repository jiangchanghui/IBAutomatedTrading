package analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.web.request.HistoricResultSet;

public class AnalyticsCache {
	public static AnalyticsCache instance = new AnalyticsCache();
	HashMap<String, Double> _RSICache;
	HashMap<String, HistoricalRsiCache> _HistRSICache;
	boolean IsApiConnected = false;
	
	public AnalyticsCache()
	{
		_RSICache = new HashMap<String,Double>();
		_HistRSICache = new HashMap<String, HistoricalRsiCache>();
	}
	public HashMap<String, HistoricalRsiCache> GetHistoricalRSIMap()
	{
		return _HistRSICache;
		
	}
	public void SetRSI(String Ticker, Double RSI) {
	_RSICache.put(Ticker, RSI);
	
	HistoricalRsiCache _tmp = _HistRSICache.get(Ticker);
	if(_tmp != null)
		_tmp.SetHistRsi(System.currentTimeMillis(), RSI);
	else
		_HistRSICache.put(Ticker, new HistoricalRsiCache(System.currentTimeMillis(),RSI));
	}
	public HistoricalRsiCache GethistoricalRsiMapByTicker(String Ticker)
	{
		return _HistRSICache.get(Ticker);
	}

	public Double GetRSI(String Ticker)
	{
		if(_RSICache.get(Ticker) != null)
			return _RSICache.get(Ticker);
		return 0.0;
	}

	public void SetConnected(boolean b) {
		IsApiConnected = b;
		
	}

	public boolean IsConnected() {
		return IsApiConnected;
	}
}
