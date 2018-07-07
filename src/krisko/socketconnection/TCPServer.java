package krisko.socketconnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServer
{
	public TCPServer(TCPServerListener listener)
	{
		listSend = new ArrayList<SendingObject>();
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
			// open serversocket
			serverSocket = new ServerSocket(port);
			
			// create clients array
			clients = new ServerClient[maxClients];
			
			// start accepting clients
			acceptClients(maxClients);
			
			// start writing thread
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
	 * Adds an object to the sending list
	 * @param obj
	 */
	public synchronized void send(Object obj)
	{
		listSend.add(new SendingObject(obj));
	}
	
	/**
	 * Sends to all given cliennts
	 * @param obj
	 * @param clientIDs
	 */
	public synchronized void sendTo(Object obj, int... clientIDs)
	{
		if(clientIDs.length == 0)
			send(obj);
		else
			listSend.add(new SendingObject(obj, SendingObject.TO_GIVEN, clientIDs));
	}
	
	/**
	 * Sends to all clients except the ones given
	 * @param obj
	 * @param clientIDs
	 */
	public synchronized void sendExcept(Object obj, int... clientIDs)
	{
		if(clientIDs.length > 0)
			listSend.add(new SendingObject(obj, SendingObject.EXCEPT_GIVEN, clientIDs));
	}
	
	private synchronized SendingObject getNextSendingObject()
	{
		return listSend.size() == 0 ? null : listSend.remove(0);
	}
	
	private void acceptClients(final int maxClients)
	{
		threadAccept = new Thread(new Runnable() {
			@Override
			public void run()
			{
				while(true)
				{
					// wait for a free slot
					int index = 0;
					while(true)
					{
						if(Thread.currentThread().isInterrupted())
							return;
						
						if(clients[index] == null)
							break;
						
						if(++index >= maxClients)
							index = 0;
					}
					
					try
					{
						// wait for client to connect
						Socket socket = serverSocket.accept();
						
						// search for first free slot
						for(int i = 0; i < clients.length; i++)
						{
							if(clients[i] == null)
							{
								index = i;
								break;
							}
						}
						
						// add client to list and start reading
						if(addClient(socket, index))
						{
							// notify
							serverListener.onClientJoined(index);
						}
					} catch(IOException ex)
					{
						break;
					}
				}
			}
		});
		
		threadAccept.start();
	}
	
	private boolean addClient(Socket socket, final int index)
	{
		try
		{
			// create output / input
			ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			output.flush();
			ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			
			// start reading thread
			Thread threadRead = new Thread(new Runnable() {
				@Override
				public void run()
				{
					while(true)
					{
						try
						{
							// read object
							Object obj = clients[index].getInput().readObject();
							
							if(obj != null) // notify
								serverListener.onMessageReceived(index, obj);
						} catch(Exception ex)
						{
							if(!Thread.currentThread().isInterrupted())
								removeClient(index, true);
							break;
						}
					}
				}
			});
			
			// create new ServerClient
			clients[index] = new ServerClient(socket, output, input, threadRead);
			
			// Start thread
			threadRead.start();
		} catch(IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Starts the writing thread
	 */
	private void startWriting()
	{
		threadWrite = new Thread(new Runnable() {
			@Override
			public void run()
			{
				SendingObject sending;
				
				while(true)
				{
					// wait for objects in the list
					while((sending = getNextSendingObject()) == null)
					{
						// check if thread is still alive
						if(Thread.currentThread().isInterrupted())
							break;
					}

					// check if thread is still alive (yes, 2 times)
					if(Thread.currentThread().isInterrupted())
						break;
					
					if(sending.state == SendingObject.TO_GIVEN)
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
								clients[id].getOutput().writeObject(sending.object);
								clients[id].getOutput().flush();
							} catch(Exception ex)
							{ }
						}
					}
					else if(sending.state == SendingObject.EXCEPT_GIVEN)
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
								clients[i].getOutput().writeObject(sending.object);
								clients[i].getOutput().flush();
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
								clients[i].getOutput().writeObject(sending.object);
								clients[i].getOutput().flush();
							} catch(Exception ex)
							{ }
						}
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
		
		clients[clientID].close();
		clients[clientID] = null;
		
		if(notify)
			serverListener.onClientDisconnect(clientID);
	}
	
	/**
	 * Closes the server
	 */
	public synchronized void close()
	{
		try
		{
			if(threadAccept != null)
				threadAccept.interrupt();
			if(threadWrite != null)
				threadWrite.interrupt();
			if(serverSocket != null)
				serverSocket.close();
			
			if(clients != null)
			{
				for(int i = 0; i < clients.length; i++)
					removeClient(i);
			}
		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
		serverSocket = null;
		threadAccept = null;
		threadWrite = null;
		clients = null;
	}
	
	/** This is the array with the connected clients. The array index equals the client id*/
	private ServerClient clients[];
	
	/** The thread that waits for clients to connect */
	private Thread threadAccept;
	
	/** The thread that writes to all clients */
	private Thread threadWrite;
	
	/** The list with objects to send */
	private ArrayList<SendingObject> listSend;
	
	/** The ServerSocket */
	private ServerSocket serverSocket;
	
	/** The ServerListener which notifies when, for example, a message is received */ 
	private TCPServerListener serverListener;
	
	// Another class
	private class SendingObject
	{
		protected SendingObject(Object obj)
		{
			this(obj, TO_ALL);
		}
		
		protected SendingObject(Object obj, int sendingState, int... ids)
		{
			object = obj;
			state = sendingState;
			clientIDs = ids;
		}
		
		protected Object object;
		protected int clientIDs[];
		
		protected int state;
		protected static final int TO_ALL = 0;
		protected static final int TO_GIVEN = 1;
		protected static final int EXCEPT_GIVEN = 2;
	}
	
	// Another class
	private class ServerClient
	{
		protected ServerClient(Socket socket, ObjectOutputStream output, ObjectInputStream input, Thread threadRead)
		{
			this.socket = socket;
			this.output = output;
			this.input = input;
			this.threadRead = threadRead;
		}
		
		protected void close()
		{
			try
			{
				if(threadRead != null)
					threadRead.interrupt();
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
		}
		
		protected ObjectOutputStream getOutput() { return output; }
		
		protected ObjectInputStream getInput() { return input; }
		
		private Socket socket;
		private ObjectOutputStream output;
		private ObjectInputStream input;
		private Thread threadRead;
	}
}