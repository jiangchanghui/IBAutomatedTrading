package apidemo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import apidemo.OrdersPanel.OrderRow;
import apidemo.OrdersPanel.OrdersModel;
import apidemo.TopModel.TopRow;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.ExecutionFilter;
import com.ib.controller.ApiController.ILiveOrderHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController.ITradeReportHandler;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.SecType;
import com.ib.initialise.IBTradingMain;
import com.reademail.main.mailReader;

public class CreateOrderFromEmail {
	private static final Logger log = Logger.getLogger( CreateOrderFromEmail.class.getName() );
	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	public void CreateOrder(String Symbol, int Quantity, Action Side, Double FFLimit)
	{
		if (Symbol==null  || Quantity == 0 || Side == null)
		{
			
			 log.log(Level.WARNING ,"Order Create failed with Symbol : {0}, Quantity : {1}, Side : {2}, FFLimit :{3}",new Object[]{Symbol,Quantity,Side.toString(),FFLimit});
			return;
		}
		
		double FarPrice =  0.0;
		
	
		
		
		NewContract contract = new NewContract();
		NewOrder order = new NewOrder();
		contract.symbol(Symbol);
		order.totalQuantity(Quantity);
		order.action(Side);
		
			
		order.orderType(OrderType.MKT);
		
		//order.lmtPrice(FarPrice);
		
		contract.secType(SecType.STK);
		contract.exchange("SMART");
		contract.currency("USD");
		
	//	if (!FatFingerViolation(contract, order,FFLimit))
		//{
		log.log(Level.INFO ,"Order being executed for {0} {1} {2} at {3}",new Object[]{Side,Quantity,Symbol,order.orderType().toString()});
		IBTradingMain.INSTANCE.controller().placeOrModifyOrder(contract, order, new IOrderHandler() {
			@Override public void orderState(NewOrderState orderState) {
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
						
					}
				});
			}
			@Override public void handle(final int errorCode, final String errorMsg) {
				
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
						log.log(Level.SEVERE ,"Order execution failed with ({0}:{1})",new Object[]{errorCode,errorMsg});						
						IBTradingMain.INSTANCE.m_errorMap.put(dateFormat.format(new Date()), errorMsg);
						
						if (errorMsg.contains("Order held"))
						{
							log.log(Level.SEVERE ,"Order is held, cancelling all open orders");
							IBTradingMain.INSTANCE.controller().cancelAllOrders();
						}
						if (errorMsg.contains("not be placed"))
						{
							IBTradingMain.INSTANCE.controller().cancelAllOrders();
						}
						
						
						
						
						
					}
				});
			}
		});
	//	}
		
	}
	private double GetFarPrice(NewContract contract,Action side)
	{
		TopRow row = new TopRow( null, contract.description() );
		//m_rows.add( row);
		IBTradingMain.INSTANCE.controller().reqTopMktData(contract, "", false, row);
	//	fireTableRowsInserted( m_rows.size() - 1, m_rows.size() - 1);
		
		if (row.m_ask >0.0 && row.m_bid >0.0)
		{
			if (side==Action.BUY)
			{
				return row.m_ask+0.02;
			}
			else
			{
				return row.m_bid-0.02;
			}
		}
		else
			return 0.0;
	}
	
	private Exec GetPosition(String Symbol)
	{
		ITradeReportHandler m_tradeReportHandler = null;
		OrdersModel m_model = new OrdersModel();
		TradesPanel m_tradesPanel = new TradesPanel();
		IBTradingMain.INSTANCE.controller().reqExecutions( new ExecutionFilter(), m_tradesPanel);
		IBTradingMain.INSTANCE.controller().reqLiveOrders( m_model);

						
		ArrayList<apidemo.TradesPanel.FullExec> _Execs = new ArrayList<apidemo.TradesPanel.FullExec>();
		
		_Execs = m_tradesPanel.getExecutions();
		
		
		
		
		for (int i=0;i< _Execs.size();i++)
		{
			if (_Execs.get(i).m_contract.symbol().equals(Symbol))
			{
				return new Exec(_Execs.get(i).m_contract.symbol(),_Execs.get(i).m_trade.m_side,_Execs.get(i).m_trade.m_shares);
			}
//		System.out.println(_Execs.get(i).m_contract.symbol());
//		System.out.println(_Execs.get(i).m_trade.m_shares);
//		System.out.println(_Execs.get(i).m_trade.m_avgPrice);
//		System.out.println(_Execs.get(i).m_trade.m_side);
		
		}
		return null;
		
	}
	public String GetPositions()
	{
		ITradeReportHandler m_tradeReportHandler = null;
		OrdersModel m_model = new OrdersModel();
		TradesPanel m_tradesPanel = new TradesPanel();
		IBTradingMain.INSTANCE.controller().reqExecutions( new ExecutionFilter(), m_tradesPanel);
		IBTradingMain.INSTANCE.controller().reqLiveOrders( m_model);

						
		ArrayList<apidemo.TradesPanel.FullExec> _Execs = new ArrayList<apidemo.TradesPanel.FullExec>();
		
		_Execs = m_tradesPanel.getExecutions();
		
		
		
		
		for (int i=0;i< _Execs.size();i++)
		{
		System.out.println(_Execs.get(i).m_contract.description().toString());
		System.out.println(_Execs.get(i).m_trade.m_shares);
		System.out.println(_Execs.get(i).m_trade.m_avgPrice);
		System.out.println(_Execs.get(i).m_trade.m_side);
		
		}
		return "null";
	}
	
	private boolean FatFingerViolation(NewContract contract, NewOrder order,Double FFLimit)
	{
		Double FarPrice = GetFarPrice(contract,order.action());
	
		Double Notional = FarPrice * order.totalQuantity();
		
		if (Notional > FFLimit)
			return true;
		else
			return false;
		
		
		
	}

	static class Exec {
		String Symbol;
		String Side;
		int Quantity;
		
		Exec(String Symbol, String Side, int Quantity)
		{
			this.Symbol = Symbol;
			this.Side = Side;
			this.Quantity = Quantity;
		}
		
		String getSymbol()
		{
			return Symbol;
		}
		String getSide()
		{
			return Side;
		}
		int getQuantity()
		{
			return Quantity;
		}
	}
		
		
}
