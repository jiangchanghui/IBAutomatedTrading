package apidemo;

import java.util.ArrayList;

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
import com.ib.sample.main;

public class CreateOrderFromEmail {

	
	public void CreateOrder(String Symbol, int Quantity, Action Side, Double FFLimit)
	{
		if (Symbol==null  || Quantity == 0 || Side == null || FFLimit == 0.0)
		{
			return;
		}
		
		double FarPrice =  0.0;
		
	
		
		
		NewContract contract = new NewContract();
		NewOrder order = new NewOrder();
		contract.symbol(Symbol);
		order.totalQuantity(Quantity);
		order.action(Side);
		
			
		order.orderType(OrderType.MKT);
		
		order.lmtPrice(FarPrice);
		
		contract.secType(SecType.STK);
		contract.exchange("SMART");
		contract.currency("USD");
		
	//	if (!FatFingerViolation(contract, order,FFLimit))
		//{
		
		main.INSTANCE.controller().placeOrModifyOrder(contract, order, new IOrderHandler() {
			@Override public void orderState(NewOrderState orderState) {
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
						
					}
				});
			}
			@Override public void handle(final int errorCode, final String errorMsg) {
				
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
						System.out.println(errorCode + " " + errorMsg);
						
						//checks for locate required and cancels the order - essentially IOC
						if (errorMsg.contains("Order held"))
						{
							main.INSTANCE.controller().cancelAllOrders();
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
		main.INSTANCE.controller().reqTopMktData(contract, "", false, row);
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
		main.INSTANCE.controller().reqExecutions( new ExecutionFilter(), m_tradesPanel);
		main.INSTANCE.controller().reqLiveOrders( m_model);

						
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
		main.INSTANCE.controller().reqExecutions( new ExecutionFilter(), m_tradesPanel);
		main.INSTANCE.controller().reqLiveOrders( m_model);

						
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
