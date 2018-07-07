package krisko.socketconnection;

public interface TCPServerListener
{
	void onClientJoined(int clientID);
	
	void onClientDisconnect(int clientID);
	
	void onMessageReceived(int clientID, Object obj);
}
