package hft.main;

import java.io.File;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Map.Entry;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.JsonObject;
import com.twitter.main.SendTweet;
import com.web.request.GetHistoricMarketData;
import com.web.request.HistoricResultSet;

import analytics.AnalyticsCache;
import analytics.HistoricalRsiCache;

public class CreateWebResponse {
	private  Logger log = Logger.getLogger( this.getClass() );
	public String GetWeb(){
		try {
		 AnalyticsCache _AnalyticsCache = new AnalyticsCache().instance;
	
		 	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
		   	docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Data");
			doc.appendChild(rootElement);
			
		 for (Entry<String, HistoricalRsiCache> entry : _AnalyticsCache.GetHistoricalRSIMap().entrySet()) {
			    String Ticker = entry.getKey();
			    
			    //Create root element
			
			//	Element rootElement = doc.createElement(Ticker);
			//	doc.appendChild(rootElement);
				//add Rsi
				Element el_rsi = doc.createElement(Ticker);
				rootElement.appendChild(el_rsi);
				
			    HistoricalRsiCache value = (HistoricalRsiCache)entry.getValue();
			   
			    for (Entry<Long, Double> entryHist : value.GetHistMap().entrySet()) {
			    	  String time = String.valueOf(entryHist.getKey());
			    	  String rsi = String.valueOf(entryHist.getValue());
			    	  JsonObject innerObject = new JsonObject();
					    innerObject.addProperty(time, rsi);
					    
					    
						Element el_time = doc.createElement("Time");
						el_time.appendChild(doc.createTextNode(time));
						el_rsi.appendChild(el_time);
						
						Element el_rsi_value = doc.createElement("RsiValue");
						el_rsi_value.appendChild(doc.createTextNode(rsi));
						el_rsi.appendChild(el_rsi_value);
					    
			    	}
		 }
	
				
				 TransformerFactory transfac = TransformerFactory.newInstance();
		            Transformer trans = transfac.newTransformer();
		           trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		            trans.setOutputProperty(OutputKeys.INDENT, "yes");

		            //create string from xml tree
		            StringWriter sw = new StringWriter();
		            StreamResult result = new StreamResult(sw);
		            DOMSource source = new DOMSource(doc);
		            trans.transform(source, result);
		            String xmlString = sw.toString();
			
				System.out.println(xmlString); 
				
			  
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			 
				

			return null;
		
				
				
		
	}

	public String GetPriceAndRsi(String Ticker) 
	{
		try {
		AnalyticsCache _AnalyticsCache = AnalyticsCache.instance;
		GetHistoricMarketData GetHistMarketData = new GetHistoricMarketData();
		HistoricResultSet Data = GetHistMarketData.GetHistoricalMarketData(Ticker);
		
		String result ="{";
		 String rsi_result="{";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));

		if (Data.m_rows.size()==0)
			return "";
		for( int i=0;i < Data.m_rows.size();i++)
		{
				
		
			result+= "["+ConvertTime(Data.m_rows.get(i).time())+","+
					Data.m_rows.get(i).open()+","+
					Data.m_rows.get(i).high()+","+
					Data.m_rows.get(i).low()+","+
					Data.m_rows.get(i).close()+"],";
			
			
			
		}
		System.out.println(Data.m_rows.size());
		result = result.substring(0, result.length() - 1)+"};";
		System.out.println(result);
		
		
		HistoricalRsiCache _H = _AnalyticsCache.GethistoricalRsiMapByTicker(Ticker);
		if (_H==null)
			return "NoRSIData";
		 for (Entry<Long, Double> e : _H.GetHistMap().entrySet()) {
		
			  rsi_result="["+e.getKey()+","+String.valueOf(e.getValue())+"],";
			 
			 
		 }
		 rsi_result = rsi_result.substring(0, rsi_result.length() - 1)+"}";
		 System.out.println(result+rsi_result);
		 return result+rsi_result;
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return null;
		
		
	}
	private long ConvertTime(long l)
	{
		return (l*1000)-14400000;
		
	}
}
