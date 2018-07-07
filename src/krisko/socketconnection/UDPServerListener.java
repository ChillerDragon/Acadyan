package krisko.socketconnection;

public interface UDPServerListener
{
	void onClientJoined(int clientID, byte[] data, int received);
	
	void onClientDisconnect(int clientID);
	
	void onMessageReceived(int clientID, byte[] data, int received);
}