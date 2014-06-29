package analytics.calculations;

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
		int size = Data.m_rows.size();
		long lastTime = Data.m_rows.get(size-1).time();
		if (_time > lastTime+300000)
		{
			//more than 5 mins from last bae in Data, need new RSI.
		Data.historicalData(new Bar(_time, 0.0, 0.0,0.0, _close, 0, 0, 0), false);
		
		
		
		ArrayList<HistDataCustom> _histData = new ArrayList<HistDataCustom>();
		
		ArrayList<Double> _UpPeriods = new ArrayList<Double>();
		ArrayList<Double> _DownPeriods = new ArrayList<Double>();
		if (Data.m_rows.size()==0)
			return 0.0;
		double _PreviousClose = Data.m_rows.get(0).close();
		double _CurrentClose = 0.0;
		
		for( int i=size-14;i < size;i++)
		{
		//	_histData.add(new HistDataCustom(Data.m_rows.get(i).time(),Data.m_rows.get(i).close()));
		
	//	Arrays.sort(_histData,HistDataCustom.SalaryComparator);
		
			_CurrentClose = Data.m_rows.get(i).close();
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
		Double _MADownPeriods = calculateAverage(_DownPeriods);
		Double _RS = _MAUpPeriods / _MADownPeriods;
		Double _RSI = 100- (100/(1+_RS));
		return _RSI;
		}
		return -1;
		
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
