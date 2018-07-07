package krisko.socketconnection;

public interface UDPClientListener
{
	/**
	 * @param data
	 * @param received - The number of received bytes
	 */
	void onMessageReceived(byte[] data, int received);
	
	void onDisconnect();
}