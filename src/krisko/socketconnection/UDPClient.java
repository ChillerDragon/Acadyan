package krisko.socketconnection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class UDPClient
{
	public UDPClient(UDPClientListener listener)
	{
		listSend = new ArrayList<byte[]>();
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
			address = InetAddress.getByName(serverIP);
			this.port = port;
			
			// create connection
			socket = new DatagramSocket();
			
			// start the reading and writing threads
			startReading();
			startWriting();
		} catch(SocketException ex)
		{
			disconnect();
			return false;
		} catch(UnknownHostException ex)
		{
			disconnect();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sends the data directly
	 * Blocks until it's finished
	 * @param data
	 */
	public synchronized void sendDirectly(byte[] data)
	{
		if(socket == null)
			return;
		
		// create packet
		DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
		
		try
		{
			// write packet
			socket.send(packet);
		} catch(IOException ex)
		{ }
	}
	
	/** Look at sendDirectly(byte[] data) */
	public synchronized void sendDirectly(String str)
	{
		sendDirectly(str.getBytes(Charset.forName("ISO-8859-1")));
	}
	
	/**
	 * Adds an object to the sending list
	 * @param obj
	 */
	public synchronized void send(byte[] data)
	{
		listSend.add(data);
	}
	
	public synchronized void send(String str)
	{
		send(str.getBytes(Charset.forName("ISO-8859-1")));
	}
	
	private synchronized byte[] getNextSendingData()
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
				DatagramPacket packet;
				
				while(true)
				{
					// create packet
					packet = new DatagramPacket(new byte[1024], 1024);
					
					try
					{
						// read packet
						socket.receive(packet);
						
						// check if the packet came from an unwanted person
//						if(packet.getAddress().equals(address) || packet.getPort() != port)
//						{
//							System.out.println("Unwanted: " +packet.getAddress() +" - " +address +"  ||  " +packet.getPort() +" - " +port);
//							continue;
//						}
						
						// notify
						clientListener.onMessageReceived(packet.getData(), packet.getLength());
					} catch(IOException ex)
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
				byte[] data;
				DatagramPacket packet;
				
				while(true)
				{
					// wait for objects in the list
					while((data = getNextSendingData()) == null)
					{
						// check if thread is still alive
						if(Thread.currentThread().isInterrupted())
							break;
					}
					
					// check if thread is still alive (yes, 2 times)
					if(Thread.currentThread().isInterrupted())
						break;
					
					// create packet
					packet = new DatagramPacket(data, data.length, address, port);
					
					try
					{
						// write packet
						socket.send(packet);
					} catch(IOException ex)
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
		if(threadRead != null)
			threadRead.interrupt();
		if(threadWrite != null)
			threadWrite.interrupt();
		if(socket != null)
			socket.close();
		
		socket = null;
		threadRead = null;
		threadWrite = null;
		address = null;
		port = 0;
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
	private ArrayList<byte[]> listSend;
	
	/** The connection between client and server */
	private DatagramSocket socket;
	
	private InetAddress address;
	private int port;
	
	/** The ClientListener which notifies when, for example, a message is received */ 
	private UDPClientListener clientListener;
}