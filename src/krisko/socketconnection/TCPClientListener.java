package krisko.socketconnection;

public interface TCPClientListener
{
	void onMessageReceived(Object obj);
	
	void onDisconnect();
}