package analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.twitter.main.SendTweet;
import com.web.request.HistoricResultSet;

public class AnalyticsCache {
	private static final Logger log = Logger.getLogger( SendTweet.class.getName() );
	public static AnalyticsCache instance = new AnalyticsCache();
	HashMap<String, Double> _RSICache;
	HashMap<String, HistoricalRsiCache> _HistRSICache;
	HashMap<String,HistoricalStochasticsCache>  _StochasticsCache;
	boolean IsApiConnected = false;
	
	public AnalyticsCache()
	{
		_RSICache = new HashMap<String,Double>();
		_HistRSICache = new HashMap<String, HistoricalRsiCache>();
		_StochasticsCache = new HashMap<String,HistoricalStochasticsCache>();
	}
	public HashMap<String, HistoricalRsiCache> GetHistoricalRSIMap()
	{
		return _HistRSICache;
	
	}
	//calcualte average size of bars for all Ticerks
	
	
	public HistoricalStochasticsCache GetHistoricalStochasticMap(String Ticker)
	{
		HistoricalStochasticsCache _tmp = _StochasticsCache.get(Ticker);
		if(_tmp != null)
			return _tmp;
		else
			_StochasticsCache.put(Ticker, new HistoricalStochasticsCache());
		return _StochasticsCache.get(Ticker);
	
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

	public void SetStochastics(String ticker, double SignalLine, double SlowSto, double A, double B)
	{
		HistoricalStochasticsCache _tmp = _StochasticsCache.get(ticker);
		if(_tmp != null)
			
			_tmp.SetHistStochastics(System.currentTimeMillis(), new StochasticsStruct(SignalLine, SlowSto,A,B));
		else
			_StochasticsCache.put(ticker, new HistoricalStochasticsCache(System.currentTimeMillis(), new StochasticsStruct(SignalLine, SlowSto,A,B)));
	}
	
	
	
	
	
	public void SetConnected(boolean b) {
		IsApiConnected = b;
		
	}

	public boolean IsConnected() {
		return IsApiConnected;
	}
}
