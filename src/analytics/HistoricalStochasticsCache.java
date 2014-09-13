package analytics;

import java.util.ArrayList;
import java.util.HashMap;

import com.ib.controller.Bar;

public class HistoricalStochasticsCache {
	//public HashMap<Long, StochasticsStruct> _HistStoCache = new HashMap<Long,StochasticsStruct>();
	public final ArrayList<StochasticsStruct> _HistStoCache = new ArrayList<StochasticsStruct>();
	public HistoricalStochasticsCache(long time, StochasticsStruct s) {
		//_HistStoCache.put(time, s);
		_HistStoCache.add(s);
	}
	public HistoricalStochasticsCache()
	{
		
	}
	public void SetHistStochastics(long time, StochasticsStruct s)
	{
	//	_HistStoCache.put(time, s);
		_HistStoCache.add(s);
	}
	public void GetHistCache(long time, double rsi)
	{
	//	_HistStoCache.put(time, rsi);
	}
	public ArrayList<StochasticsStruct> GetHistMap()
	{
		return _HistStoCache;
	}
}
