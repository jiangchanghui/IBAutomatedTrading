package hft.main;

public class LivePositionHandler extends Thread{

	Cache _PositionCache;
	public LivePositionHandler()
	{
		_PositionCache = Cache.instance;
		
		
		
	}
	
	//somehow call this each time a position changes.
	
	public void OnPositionChanged()
	{
		
		
		//Check all open positions have associated orders.
		
		//new position created and so need to place limit.
		
		//get execution price and place limit at average bar size above.
				
		
		
	}
	
	
	
	

}
