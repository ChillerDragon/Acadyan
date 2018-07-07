package krisko.socketconnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class TCPClient
{
	public TCPClient(TCPClientListener listener)
	{
		listSend = new ArrayList<Object>();
		clientListener = listener;
	}
	
	/**
	 * Tries to connect to the server with the given IP and Port
	 * Starts reading after connecting
	 * @param serverIP
	 * @param port
	 * @return true, when the connection was successful, false, otherwise
	 */
	public boolean connect(String serverIP, int port)
	{
		disconnect();
		
		try
		{
			// create connection
			socket = new Socket(serverIP, port);
			
			// get outputstream
			output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			output.flush();
			
			// get inputstream
			input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		} catch(UnknownHostException ex)
		{
			disconnect();
			return false;
		} catch(IOException ex)
		{
			disconnect();
			return false;
		}
		
		// start the reading and writing threads
		startReading();
		startWriting();
		
		return true;
	}
	
	/**
	 * Adds an object to the sending list
	 * @param obj
	 */
	public synchronized void send(Object obj)
	{
		listSend.add(obj);
	}
	
	private synchronized Object getNextSendingObject()
	{
		return listSend.size() == 0 ? null : listSend.remove(0);
	}
	
	/**
	 * Starts the reading Thread
	 */
	private void startReading()
	{
		threadRead = new Thread(new Runnable() {
			@Override
			public void run()
			{
				while(true)
				{
					try
					{
						// read object
						Object obj = input.readObject();
						
						if(obj != null) // notify
							clientListener.onMessageReceived(obj);
					} catch(Exception ex)
					{
						if(!Thread.currentThread().isInterrupted())
							disconnect(true);
						break;
					}
				}
			}
		});
		
		threadRead.start();
	}
	
	/**
	 * Starts the writing Thread
	 */
	private void startWriting()
	{
		threadWrite = new Thread(new Runnable() {
			@Override
			public void run()
			{
				Object obj;
				
				while(true)
				{
					// wait for objects in the list
					while((obj = getNextSendingObject()) == null)
					{
						// check if thread is still alive
						if(Thread.currentThread().isInterrupted())
							break;
					}

					// check if thread is still alive (yes, 2 times)
					if(Thread.currentThread().isInterrupted())
						break;
					
					try
					{
						// write object
						output.writeObject(obj);
						output.flush();
					} catch(Exception ex)
					{
						if(!Thread.currentThread().isInterrupted())
							disconnect(true);
						break;
					}
				}
			}
		});
		
		threadWrite.start();
	}
	
	/** Disconnects from the server if a connection is active and sets everything to null
	 * if 'notify' is true, then the method 'onDisconnect' will be activated
	 */
	public synchronized void disconnect(boolean notify)
	{
		try
		{
			if(threadRead != null)
				threadRead.interrupt();
			if(threadWrite != null)
				threadWrite.interrupt();
			if(socket != null)
				socket.close();
		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
		socket = null;
		input = null;
		output = null;
		threadRead = null;
		threadWrite = null;
		listSend.clear();
		
		if(notify)
			clientListener.onDisconnect();
	}
	
	/** Disconnects without notifying */
	public synchronized void disconnect()
	{
		disconnect(false);
	}
	
	/** The thread that reads incoming data from the server */
	private Thread threadRead;
	
	/** The thread that writes data to the server */
	private Thread threadWrite;
	
	/** The list with objects to send */
	private ArrayList<Object> listSend;
	
	/** The connection between client and server */
	private Socket socket;
	
	// Input and Output streams
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	/** The ClientListener which notifies when, for example, a message is received */ 
	private TCPClientListener clientListener;
}