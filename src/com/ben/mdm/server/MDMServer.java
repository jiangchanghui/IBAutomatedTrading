package com.ben.mdm.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MDMServer extends Thread{
	 ArrayList<ClientHandler> al;
	public void run()
	{
		boolean keepGoing = true;
		try {
			ServerSocket serverSocket = new ServerSocket(5123);
			ClientHandler _handler = new ClientHandler();
			while(true) 
			{
				// format message saying we are waiting
				
				
				Socket socket = serverSocket.accept(); 
				_handler.Start(socket);
				// accept connection
				// if I was asked to stop
				  // make a thread of it
				
				
			}
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
