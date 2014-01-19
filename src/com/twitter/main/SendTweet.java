package com.twitter.main;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import com.reademail.main.*;
public class SendTweet {

	public SendTweet()
	{
		
	}
	
	

public void SendNewTweet(String message , OrderTemplate _OrderTemplate)
	
 {
		try{
		   TwitterFactory factory = new TwitterFactory();
		    AccessToken accessToken = loadAccessToken();
		    Twitter twitter = factory.getInstance();
		    twitter.setOAuthConsumer("uElgz3OkbjbZZ2edhxlw", "PQVMxp8W2oB0qinvQurRfDPfioVjG8vYg1hCL0tYnI");
		    twitter.setOAuthAccessToken(accessToken);
		  
		    
		    Status status = twitter.updateStatus(ComposeMessage(message,_OrderTemplate));
		    System.out.println("Successfully updated the status to [" + status.getText() + "].");
		    
		  }
		catch (Exception e)
		{
			 System.out.println(e.toString());
		}
 }
		  private static AccessToken loadAccessToken(){
		    String token = "2300082091-SXJzIyt7ZYMSI86kqAFopxKoOy27fr1MnlLFmo5";
		    String tokenSecret = "iKGYQzwDPDCazZN0iXQUwUbf23CCHhXqdEEEIFTGSvZ56";
		    return new AccessToken(token, tokenSecret);
		  }
		
		private String ComposeMessage(String message , OrderTemplate _OrderTemplate)
		{
			String response = message + " -> "+  _OrderTemplate.getSide()+" "+_OrderTemplate.getQuantity()+" "+_OrderTemplate.getTicker();
			
			
			return response;
			
			
			
		}
		

	}


