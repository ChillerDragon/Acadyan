package krisko.socketconnection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class UDPServer
{
	public UDPServer(UDPServerListener listener)
	{
		listSend = new ArrayList<SendingData>();
		serverListener = listener;
	}
	
	/**
	 * Opens the server with the specific port and starts accepting clients
	 * @param port
	 * @return true, when the server could be opened, false, otherwise
	 */
	public boolean open(int port, int maxClients)
	{
		close();
		
		// Don't create the server
		if(maxClients <= 0)
			return false;
		
		try
		{
			// create socket
			socket = new DatagramSocket(port);
			
			// create clients array
			clients = new ServerClient[maxClients];
			
			// start the reading and writing threads
			startReading();
			startWriting();
			
			return true;
		} catch(IOException ex)
		{
			// port already in use
			ex.printStackTrace();
			close();
			return false;
		}
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
		
		for(int i = 0; i < clients.length; i++)
		{
			if(clients[i] == null)
				continue;
			
			try
			{
				DatagramPacket packet = new DatagramPacket(data, data.length, clients[i].address, clients[i].port);
				socket.send(packet);
			} catch(Exception ex)
			{ }
		}
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
		listSend.add(new SendingData(data));
	}
	
	public synchronized void send(String str)
	{
		send(str.getBytes(Charset.forName("ISO-8859-1")));
	}
	
	/**
	 * Sends to all given cliennts
	 * @param obj
	 * @param clientIDs
	 */
	public synchronized void sendTo(byte[] data, int... clientIDs)
	{
		if(clientIDs.length == 0)
			send(data);
		else
			listSend.add(new SendingData(data, SendingData.TO_GIVEN, clientIDs));
	}
	
	public synchronized void sendTo(String str, int... clientIDs)
	{
		sendTo(str.getBytes(Charset.forName("ISO-8859-1")), clientIDs);
	}
	
	/**
	 * Sends to all clients except the ones given
	 * @param obj
	 * @param clientIDs
	 */
	public synchronized void sendExcept(byte[] data, int... clientIDs)
	{
		if(clientIDs.length > 0)
			listSend.add(new SendingData(data, SendingData.EXCEPT_GIVEN, clientIDs));
	}
	
	public synchronized void sendExcept(String str, int... clientIDs)
	{
		sendExcept(str.getBytes(Charset.forName("ISO-8859-1")), clientIDs);
	}
	
	private synchronized SendingData getNextSendingData()
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
				int index;
				boolean clientInList;
				
				while(true)
				{
					// create packet
					packet = new DatagramPacket(new byte[1024], 1024);
					index = -1;
					clientInList = false;
					
					try
					{
						// read packet
						socket.receive(packet);
						
						// check if the packet came from someone that is not in the client list and if the list is already full
						for(int i = 0; i < clients.length; i++)
						{
							if(clients[i] != null)
							{
								if(packet.getAddress().equals(clients[i].address) && packet.getPort() == clients[i].port)
								{
									index = i;
									clientInList = true;
									break;
								}
							}
							else
							{
								if(index == -1)
									index = i;
							}
						}
						
						// add client
						if(!clientInList && index >= 0)
						{
							clients[index] = new ServerClient(packet.getAddress(), packet.getPort());
							serverListener.onClientJoined(index, packet.getData(), packet.getLength());
						}
						else if(clientInList) // notify
							serverListener.onMessageReceived(index, packet.getData(), packet.getLength());
						
						// if client is not in the list and the server is full, just don't respond
					} catch(IOException ex)
					{
						if(Thread.currentThread().isInterrupted())
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
				SendingData sending;
				DatagramPacket packet;
				
				while(true)
				{
					// wait for objects in the list
					while((sending = getNextSendingData()) == null)
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
						if(sending.state == SendingData.TO_GIVEN)
						{
							// write object to given clients
							int id;
							for(int i = 0; i < sending.clientIDs.length; i++)
							{
								id = sending.clientIDs[i];
								
								if(clients[id] == null)
									continue;
								
								try
								{
									packet = new DatagramPacket(sending.data, sending.data.length, clients[id].address, clients[id].port);
									socket.send(packet);
								} catch(IOException ex)
								{ }
							}
						}
						else if(sending.state == SendingData.EXCEPT_GIVEN)
						{
							// write object to all clients except the given ones
							boolean isGivenID;
							for(int i = 0; i < clients.length; i++)
							{
								if(clients[i] == null)
									continue;
								
								isGivenID = false;
								for(int j = 0; j < sending.clientIDs.length; j++)
								{
									if(sending.clientIDs[j] == i)
									{
										isGivenID = true;
										break;
									}
								}
								
								if(isGivenID)
									continue;
								
								try
								{
									packet = new DatagramPacket(sending.data, sending.data.length, clients[i].address, clients[i].port);
									socket.send(packet);
								} catch(Exception ex)
								{ }
							}
						}
						else
						{
							// write object to all connected clients
							for(int i = 0; i < clients.length; i++)
							{
								if(clients[i] == null)
									continue;
								
								try
								{
									packet = new DatagramPacket(sending.data, sending.data.length, clients[i].address, clients[i].port);
									socket.send(packet);
								} catch(Exception ex)
								{ }
							}
						}
					} catch(NullPointerException ex)
					{
						// happens somethings, when you close the game (line 252)
					}
				}
			}
		});
		
		threadWrite.start();
	}
	
	public synchronized void removeClient(int clientID)
	{
		removeClient(clientID, false);
	}
	
	public synchronized void removeClient(int clientID, boolean notify)
	{
		if(clients == null || clients[clientID] == null)
			return;
		
		clients[clientID] = null;
		
		if(notify)
			serverListener.onClientDisconnect(clientID);
	}
	
	/**
	 * Closes the server
	 */
	public synchronized void close()
	{
		if(threadRead != null)
			threadRead.interrupt();
		if(threadWrite != null)
			threadWrite.interrupt();
		if(socket != null)
			socket.close();
		
		if(clients != null)
		{
			for(int i = 0; i < clients.length; i++)
				removeClient(i);
		}
		
		socket = null;
		threadRead = null;
		threadWrite = null;
		clients = null;
	}
	
	/** This is the array with the connected clients. The array index equals the client id*/
	private ServerClient clients[];
	
	/** The thread that reads incoming data from the server */
	private Thread threadRead;
	
	/** The thread that writes data to the server */
	private Thread threadWrite;
	
	/** The list with objects to send */
	private ArrayList<SendingData> listSend;
	
	/** The connection between client and server */
	private DatagramSocket socket;
	
	/** The ClientListener which notifies when, for example, a message is received */ 
	private UDPServerListener serverListener;
	
	// Another class
	private class SendingData
	{
		protected SendingData(byte[] data)
		{
			this(data, TO_ALL);
		}
		
		protected SendingData(byte[] data, int sendingState, int... ids)
		{
			this.data = data;
			state = sendingState;
			clientIDs = ids;
		}
		
		protected byte[] data;
		protected int clientIDs[];
		
		protected int state;
		protected static final int TO_ALL = 0;
		protected static final int TO_GIVEN = 1;
		protected static final int EXCEPT_GIVEN = 2;
	}
	
	// Another class
	private class ServerClient
	{
		protected ServerClient(InetAddress address, int port)
		{
			this.address = address;
			this.port = port;
		}
		
		private InetAddress address;
		private int port;
	}
}