package analytics;




import hft.main.QueueHandler;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.benberg.struct.MarketDataTick;
import com.benberg.struct.NewOrderRequest;
import com.ib.cache.CommonCache;
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

public class AdminPanel  extends NewTabPanel{
    private final NewTabbedPanel m_tabbedPanel = new NewTabbedPanel();
    private final NewContract m_contract = new NewContract();
    public AdminPanel() {
    //	m_requestPanel.addTab( "Top Market Data", new TopRequestPanel() );
	//	m_requestPanel.addTab( "Deep Book", new DeepRequestPanel() );
    	m_tabbedPanel.addTab("AlgoAdmin",new AlgoAdmin() );
    	m_tabbedPanel.addTab("LevelOneSnapshotAdmin",new LevelOneSnapshotAdmin() );
    	
    	
		setLayout( new BorderLayout() );
		add(m_tabbedPanel, BorderLayout.NORTH);
	 
			
        
        
    }
    private class AlgoAdmin extends JPanel {
        
     
         UpperField m_symbol = new UpperField();
         UpperField m_Qty = new UpperField();
         UpperField m_Ratio = new UpperField();
         UpperField m_Price = new UpperField();
         AlgoAdmin() {
        	 HtmlButton setMarketData = new HtmlButton( "SetSymbolLastPrice") {
                 @Override protected void actionPerformed() {
              QueueHandler.INSTANCE.SendToMarketDataTickQueue(new MarketDataTick(m_symbol.getText(), new com.ib.controller.Bar(0000,0,0,0,m_Price.getDouble(),0,0,0)));
              
                     
                     
                 }
             };
            HtmlButton SetQty = new HtmlButton( "Set HFT Qty") {
                @Override protected void actionPerformed() {
                	if (m_Qty.getInt() != 0)
    					com.ib.cache.CommonCache.instance.SetHftQty(m_Qty.getInt());
                    
                    
                }

            };
            HtmlButton SetRatio = new HtmlButton( "Set HFT Ratio") {
                @Override protected void actionPerformed() {
               //     onsetPos();
                	if (m_Ratio.getDouble() != 0)
    					com.ib.cache.CommonCache.instance.SetHftRatio(m_Ratio.getDouble());
                    
                }
            };
           
            VerticalPanel butPanel = new VerticalPanel();
            butPanel.add( SetQty);
            butPanel.add( SetRatio);
            butPanel.add(setMarketData);
                       
            VerticalPanel paramPanel = new VerticalPanel();
            paramPanel.add("MDM Symbol",m_symbol);
            paramPanel.add("MDM Price",m_Price);
            paramPanel.add( "Set hft trade Qty", m_Qty );
            paramPanel.add( "SetCloseOrderRatioToAvgBarSize", m_Ratio );          
            JPanel rightPanel = new StackPanel();
            rightPanel.add( paramPanel);
            
            setLayout( new BoxLayout( this, BoxLayout.X_AXIS) );
          
            
            add (paramPanel);
            add( butPanel);
        }

       
    }
    
    private class LevelOneSnapshotAdmin extends JPanel {
        
        
        UpperField m_symbol = new UpperField();
        UpperField m_bid = new UpperField();
        UpperField m_ask = new UpperField();
        UpperField m_last = new UpperField();
        UpperField m_close = new UpperField();
        LevelOneSnapshotAdmin() {
       	 HtmlButton setMarketData = new HtmlButton( "UpdateSnapshot") {
                @Override protected void actionPerformed() {
        //     QueueHandler.INSTANCE.SendToMarketDataTickQueue(new MarketDataTick(m_symbol.getText(), new com.ib.controller.Bar(0000,0,0,0,m_Price.getDouble(),0,0,0)));
             
                    
                    
                }
            };
         
        
          
           VerticalPanel butPanel = new VerticalPanel();
            butPanel.add(setMarketData);
                      
           VerticalPanel paramPanel = new VerticalPanel();
           paramPanel.add("Ticker",m_symbol);
           paramPanel.add("bid",m_bid);
           paramPanel.add("ask", m_ask );
           paramPanel.add("last", m_last );          
           JPanel rightPanel = new StackPanel();
           rightPanel.add( paramPanel);
           
           setLayout( new BoxLayout( this, BoxLayout.X_AXIS) );
         
           
           add (paramPanel);
           add( butPanel);
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
