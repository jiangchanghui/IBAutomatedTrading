package analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


import com.ib.controller.Bar;
import com.web.request.GetHistoricMarketData;
import com.web.request.HistoricResultSet;

public class RSICalculator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	

	}

	public double CalculateRsi(String _Ticker, long _time, double _close) {
		try{
			System.out.println("Here");
		GetHistoricMarketData GetHistMarketData = new GetHistoricMarketData();
		
		
		
		return RSIWorker(GetHistMarketData.GetHistoricalMarketData(_Ticker),_time,_close);
		
			
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	private double RSIWorker(HistoricResultSet Data,long _time, double _close)
	{
		//is _time > 5 minutes from last time in Data
		
		AnalyticsCache _AnalyticsCache = AnalyticsCache.instance;
		
		int size = Data.m_rows.size();
		long lastTime = Data.m_rows.get(size-1).time();
		double CurrentRSI = _AnalyticsCache.GetRSI(Data.GetTicker());
		System.out.println("Last : "+_close);
		System.out.println((_time - lastTime));
		if((_time < (lastTime+60)) && (CurrentRSI > 0))
			return CurrentRSI;
		else if (CurrentRSI > 0)
			Data.historicalData(new Bar(_time, 0.0, 0.0,0.0, _close, 0, 0, 0), false);
		
			//more than 5 mins from last bae in Data, need new RSI.
		
		
		
		
		ArrayList<HistDataCustom> _histData = new ArrayList<HistDataCustom>();
		
		ArrayList<Double> _UpPeriods = new ArrayList<Double>();
		ArrayList<Double> _DownPeriods = new ArrayList<Double>();
		if (Data.m_rows.size()==0)
			return 0.0;
		double _PreviousClose = Data.m_rows.get(size-15).close();
		double _CurrentClose = 0.0;
		System.out.println("Running RSI calc");
		for( int i=size-14;i < size;i++)
		{
			
			_CurrentClose = Data.m_rows.get(i).close();
			System.out.println(_CurrentClose);
			if (_CurrentClose > _PreviousClose) //Up bar
				_UpPeriods.add(_CurrentClose-_PreviousClose);
			else if (_CurrentClose < _PreviousClose) //down bar
				_DownPeriods.add(_PreviousClose-_CurrentClose);
			else //no change
			{
				_UpPeriods.add(0.0);
				_DownPeriods.add(0.0);
			}
			_PreviousClose =_CurrentClose;
		}
		
		Double _MAUpPeriods = calculateAverage(_UpPeriods);
		System.out.println("MA up "+_MAUpPeriods);
		Double _MADownPeriods = calculateAverage(_DownPeriods);
		System.out.println("MA down "+_MADownPeriods);
		Double _RS = _MAUpPeriods / _MADownPeriods;
		Double _RSI = 100- (100/(1+_RS));
		System.out.println("End calc");
		_AnalyticsCache.SetRSI(Data.GetTicker(), _RSI);
		System.out.println("RSI new calc to be : "+_RSI);
		return _RSI;
		}
		
		
	private double calculateAverage(ArrayList<Double> marks) {
		  Double sum = 0.0;
		  if(!marks.isEmpty()) {
		    for (Double mark : marks) {
		        sum += mark;
		    }
		    return sum.doubleValue() / marks.size();
		  }
		  return sum;
		}
	
	private  class HistDataCustom {
		private long time;
		private double close;
		
		public HistDataCustom(long time, double close) {
			this.time = time;
			this.close = close;
		}

	public long getTime()
	{
		return time;
		
	}
	/*
	 public static  Comparator<HistDataCustom> SalaryComparator = new Comparator<HistDataCustom>() {
		 
	        @Override
	        public int compare(HistDataCustom e1, HistDataCustom e2) {
	            return (int) (e1.getTime() - e2.getTime());
	        }
	    };

	@Override
	public int compareTo(HistDataCustom o) {
		// TODO Auto-generated method stub
		return 0;
	}
	*/
	
}
	}
