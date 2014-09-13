package analytics;

import java.util.HashMap;

public class HistoricalRsiCache {
	
		public HashMap<Long, Double> _HistRSICache = new HashMap<Long,Double>();
		
		public HistoricalRsiCache(long time, Double RSI) {
			_HistRSICache.put(time, RSI);
		}

		public void SetHistRsi(long time, double rsi)
		{
			_HistRSICache.put(time, rsi);
		}
		public void GetRSIFrom(long time, double rsi)
		{
			_HistRSICache.put(time, rsi);
		}
		public HashMap<Long, Double> GetHistMap()
		{
			return _HistRSICache;
		}
	}


