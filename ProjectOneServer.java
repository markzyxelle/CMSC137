import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
public class ProjectOneServer{
	static ByteArrayInputStream b_in;
	static DatagramPacket dgram;
	static ObjectInputStream o_in;
		
	static InetAddress IPAddress;
	static ByteArrayOutputStream baos;
	static ObjectOutputStream oos;
	static DatagramPacket sendPacket;
	static TCPPacket receiveTCP;
	static TCPPacket sendTCP;
	static Random r;
	static int port;
	static byte[] sendData;
      	static byte[] receiveData;
      	static DatagramSocket sendSocket;
	static DatagramSocket receiveSocket;
	static String message;
	static boolean terminateSend = false;

	public static void main(String args[]) throws Exception{
		IPAddress = InetAddress.getByName("localhost");
		r = new Random();
		port = 9871;
		sendData = new byte[65535];
	      	receiveData = new byte[65535];
	      	sendSocket = new DatagramSocket();
		receiveSocket = new DatagramSocket(port);
		Thread receiver = new Thread(){
			public void run() {
				try{
					while(true){
						//receive
						b_in = new ByteArrayInputStream(receiveData);
						dgram = new DatagramPacket(receiveData, receiveData.length);

						receiveSocket.receive(dgram);
						if(r.nextInt(100) > 75) continue;
						o_in = new ObjectInputStream(b_in);
						dgram.setLength(receiveData.length);
						receiveMessage(o_in);
						//end receive
					}
				}catch(Exception e){}
			}
		};
		//3-way handshake
		System.out.println("Start 3-way Handshake");
		try{
			//receive
			b_in = new ByteArrayInputStream(receiveData);
			dgram = new DatagramPacket(receiveData, receiveData.length);
			receiveSocket.receive(dgram);
			o_in = new ObjectInputStream(b_in);
			dgram.setLength(receiveData.length);
			//receiveMessage(o_in);
			TimeUnit.MILLISECONDS.sleep(2000);
			receiveTCP = (TCPPacket) o_in.readObject();
			System.out.println("Packet Received");
			receiveTCP.printDetails();
			b_in.reset();
			//end receive
		}catch(Exception e){}
		try{
			//send
			baos = new ByteArrayOutputStream(65535);
		   	oos = new ObjectOutputStream(baos);
		   	sendTCP = new TCPPacket(1, r.nextInt(1000000), 1, receiveTCP.synnum+1, 0, 0, "");
		   	oos.writeObject(sendTCP);
		   	oos.flush();
		   	sendData = baos.toByteArray();
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9870);
			sendSocket.send(sendPacket);
			//end send
		}catch(Exception e){}
		try{
			//receive
			b_in = new ByteArrayInputStream(receiveData);
			dgram = new DatagramPacket(receiveData, receiveData.length);
			receiveSocket.receive(dgram);
			o_in = new ObjectInputStream(b_in);
			dgram.setLength(receiveData.length);
			//receiveMessage(o_in);
			TimeUnit.MILLISECONDS.sleep(2000);
			receiveTCP = (TCPPacket) o_in.readObject();
			System.out.println("Packet Received");
			receiveTCP.printDetails();
			b_in.reset();
			//end receive
		}catch(Exception e){}
		if(sendTCP.synnum+1 != receiveTCP.acknum) return;
		sendSocket.send(sendPacket);
		System.out.println("3-way Handshake Suceeded");
		
		receiver.start();
		while(terminateSend == false) System.out.print("");
		System.out.println("Closing...................");
		TimeUnit.MILLISECONDS.sleep(10000);
		System.exit(0);
	}
	
	public static void receiveMessage(ObjectInputStream o_in) throws InterruptedException {
		final ObjectInputStream o = o_in;
		new Thread()
		{
		    public void run() {
		    	TCPPacket recTCP; 
		    	TCPPacket sTCP;
		    	ByteArrayOutputStream tbaos;
		    	ObjectOutputStream toos;
		    	byte[] tsendData = new byte[65535];
		    	DatagramPacket tsendPacket;
		    	try{
		    		DatagramSocket tsendSocket = new DatagramSocket();
		    		recTCP = (TCPPacket) o.readObject();
		    		Thread.sleep(2000);
				b_in.reset();
				System.out.println("Packet Received");
				recTCP.printDetails();
				if(recTCP.finflag == 1){
					System.out.println("-------------------------------------------------");
					tbaos = new ByteArrayOutputStream(65535);
					toos = new ObjectOutputStream(tbaos);
					sTCP = new TCPPacket(0, 0, 1, 0, 0, 0, "");
					toos.writeObject(sTCP);
					toos.flush();
					tsendData = tbaos.toByteArray();
					tsendPacket = new DatagramPacket(tsendData, tsendData.length, IPAddress, 9870);
					tsendSocket.send(tsendPacket);
					tbaos = new ByteArrayOutputStream(65535);
					toos = new ObjectOutputStream(tbaos);
					sTCP = new TCPPacket(0, 0, 0, 0, 0, 1, "");
					toos.writeObject(sTCP);
					toos.flush();
					tsendData = tbaos.toByteArray();
					tsendPacket = new DatagramPacket(tsendData, tsendData.length, IPAddress, 9870);
					tsendSocket.send(tsendPacket);
				}
				else if(recTCP.ackflag == 1){
					terminateSend = true;
				}
				else{
					System.out.println(terminateSend);
					if(terminateSend == false){
						tbaos = new ByteArrayOutputStream(65535);
						toos = new ObjectOutputStream(tbaos);
						sTCP = new TCPPacket(0, 0, 1, recTCP.synnum+1, 2, 0, "");
						toos.writeObject(sTCP);
						toos.flush();
						tsendData = tbaos.toByteArray();
						tsendPacket = new DatagramPacket(tsendData, tsendData.length, IPAddress, 9870);
						tsendSocket.send(tsendPacket);
					}
				}
			}catch(Exception e){
				System.out.println("Error");
			}
		    }
		}.start();
	}

}
