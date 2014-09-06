package com.posttrade.main;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimerTask;

import apidemo.TradesPanel;
import apidemo.util.Util;

import com.ib.client.ExecutionFilter;
import com.ib.sample.IBTradingMain;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class RecordDailyTrades extends TimerTask {
	
	private java.sql.Connection connect;
	private java.sql.Statement statement;
	private ResultSet resultSet;
    public void run() {
    	 
    	//Connect to db
    	try {
			connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/feedback?"
			              + "user="+Util.INSTANCE.DBUsername+"&password="+Util.INSTANCE.DBPassword);
		 

    	      // statements allow to issue SQL queries to the database
    	statement = connect.createStatement();
    	      // resultSet gets the result of the SQL query
    	resultSet = statement
    	          .executeQuery("select * from FEEDBACK.COMMENTS");
    	      WriteData();
    	      
    	}      catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    	//Create entry for days pnl
    	
    	//Create entry for each positions pnl referencing the entry above in another table.
    	
    	
    	//Tweet values
    	
    	
    }
    
    public void WriteData()
    {
    	
    	  PreparedStatement preparedStatement;
		try {
			preparedStatement = connect
			          .prepareStatement("insert into  FEEDBACK.COMMENTS values (default, ?, ?, ?, ? , ?, ?)");
		    // parameters start with 1
    	      preparedStatement.setString(1, "Test");
    	      preparedStatement.setString(2, "TestEmail");
    	      preparedStatement.setString(3, "TestWebpage");
    	      preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
    	      preparedStatement.setString(5, "TestSummary");
    	      preparedStatement.setString(6, "TestComment");
    	      preparedStatement.executeUpdate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	      // "myuser, webpage, datum, summary, COMMENTS from FEEDBACK.COMMENTS");
    	  
    }
}
		
		
		
		
		
	