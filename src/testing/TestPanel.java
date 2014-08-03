package testing;

import hft.main.Cache;
import hft.main.QueueHandler;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.benberg.struct.MarketDataTick;
import com.benberg.struct.NewOrderRequest;
import com.ib.client.Contract;
import com.ib.controller.NewContract;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;

import apidemo.ContractPanel;

import apidemo.util.HtmlButton;
import apidemo.util.NewTabbedPanel;
import apidemo.util.UpperField;
import apidemo.util.VerticalPanel;
import apidemo.util.NewTabbedPanel.NewTabPanel;
import apidemo.util.VerticalPanel.StackPanel;

public class TestPanel extends NewTabPanel{
	private final NewTabbedPanel m_requestPanel = new NewTabbedPanel();
	private final NewContract m_contract = new NewContract();
	public TestPanel() {
		
		m_requestPanel.addTab( "Invoke", new Panel() );	
		
		setLayout( new BorderLayout() );
		add( m_requestPanel, BorderLayout.NORTH);
		
		
	}
	private class Panel extends JPanel {
		
		final UpperField m_Price = new UpperField();
		final UpperField m_position = new UpperField();
		final UpperField m_avgpx = new UpperField();
		final ContractPanel m_contractPanel = new ContractPanel(m_contract);
		 UpperField m_symbol = new UpperField();
		 
		Panel() {
			HtmlButton setPrice = new HtmlButton( "Set Price") {
				@Override protected void actionPerformed() {
					onsetPrice();
					
					
				}
			};
			HtmlButton setPos = new HtmlButton( "Set Pos") {
				@Override protected void actionPerformed() {
					onsetPos();
					
					
				}
			};
			HtmlButton setMarketable = new HtmlButton( "Analytics Marketable") {
				@Override protected void actionPerformed() {
					onSetMarketable();
					
					
				}
			};
			VerticalPanel butPanel = new VerticalPanel();
			butPanel.add( setPrice);
			
			VerticalPanel otherPanel = new VerticalPanel();
			otherPanel.add(setMarketable);
			
			VerticalPanel paramPanel = new VerticalPanel();
		//	paramPanel.add( "Ticker", m_symbol );
			paramPanel.add( "SetPrice", m_Price );
			paramPanel.add(setPrice);
			paramPanel.add( "SetPosition", m_position);
			paramPanel.add( "SetAvgPx", m_avgpx);
			paramPanel.add(setPos);
			
			JPanel rightPanel = new StackPanel();
			rightPanel.add( paramPanel);
			
			setLayout( new BoxLayout( this, BoxLayout.X_AXIS) );
			add( m_contractPanel);
			add( Box.createHorizontalStrut(20) );
			add( rightPanel);
			add( butPanel);
			add(otherPanel);
		}

		protected void onsetPrice() {
			m_contractPanel.onOK();
			System.out.println(m_contract.symbol().toUpperCase());
			System.out.println(m_Price.getDouble());
			Cache.instance.SetLastPx(m_contract.symbol().toUpperCase(), m_Price.getDouble());
			
		}
		protected void onsetPos() {
			
			Cache.instance.GetAllPositions().position("none", m_contract,m_position.getInt(),m_avgpx.getDouble());
			
		}
		
		protected void onSetMarketable(){
			m_contractPanel.onOK();
			QueueHandler.instance.SendToNewOrderQueue(new NewOrderRequest(m_contract.symbol(),100,OrderType.MKT,0.0,Action.BUY));
			
			
		}
		
		
		
		
		
	}
	
	@Override
	public void activated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closed() {
		// TODO Auto-generated method stub
		
	}

}
