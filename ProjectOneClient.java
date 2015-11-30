import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
public class ProjectOneClient{
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
	static int acknowledged = 0;
	static String message = "This is the message to be sent using TCP";
	static boolean terminateSend = false;

	public static void main(String args[]) throws Exception{
		IPAddress = InetAddress.getByName("localhost");
		r = new Random();
		port = 9870;
		sendData = new byte[65535];
	      	receiveData = new byte[65535];
	      	sendSocket = new DatagramSocket();
		receiveSocket = new DatagramSocket(port);
		int windowsize = 8;
		Thread receiver = new Thread(){
			public void run() {
				try{
					while(true){
						//receive
						b_in = new ByteArrayInputStream(receiveData);
						dgram = new DatagramPacket(receiveData, receiveData.length);

						receiveSocket.receive(dgram);
						o_in = new ObjectInputStream(b_in);
						dgram.setLength(receiveData.length);
						receiveMessage(o_in);
						//end receive
					}
				}catch(Exception e){}
			}
		};
		//3-way handshake
		try{
			//send
			baos = new ByteArrayOutputStream(65535);
		   	oos = new ObjectOutputStream(baos);
		   	sendTCP = new TCPPacket(1, r.nextInt(1000000), 0, 0, 0, 0, "");
		   	oos.writeObject(sendTCP);
		   	oos.flush();
		   	sendData = baos.toByteArray();
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9871);
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
			TimeUnit.MILLISECONDS.sleep(2000);
			receiveTCP = (TCPPacket) o_in.readObject();
			System.out.println("Packet Received");
			receiveTCP.printDetails();
			b_in.reset();
			//end receive
		}catch(Exception e){}
		if(sendTCP.synnum+1 != receiveTCP.acknum) {System.out.println("dfsdfsdf");return;}
		try{
			//send
			baos = new ByteArrayOutputStream(65535);
		   	oos = new ObjectOutputStream(baos);
		   	sendTCP = new TCPPacket(0, 0, 1, receiveTCP.synnum+1, 0, 0, "");
		   	oos.writeObject(sendTCP);
		   	oos.flush();
		   	sendData = baos.toByteArray();
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9871);
			sendSocket.send(sendPacket);
			//end send
		}catch(Exception e){}
		receiveSocket.receive(dgram); //to block
		System.out.println("3-way Handshake Suceeded");
		int syn = 0;
		acknowledged = 1;
		receiver.start();
		System.out.println(message.length());
		while(syn <= message.length()-1){
			//send
			//System.out.println(message.substring(syn,syn+2));
			System.out.println("syn = "+syn);
			Thread.sleep(100);
			baos = new ByteArrayOutputStream(65535);
		   	oos = new ObjectOutputStream(baos);
		   	try{
		   		sendTCP = new TCPPacket(1, syn+2, 0, 0, 2, 0, message.substring(syn,syn+2));
		   	}catch(Exception e){
		   		sendTCP = new TCPPacket(1, syn+2, 0, 0, 2, 0, message.substring(syn));
		   	}
		   	oos.writeObject(sendTCP);
		   	oos.flush();
		   	sendData = baos.toByteArray();
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9871);
			sendMessage(sendPacket, sendTCP);
			syn += 2;
			while(syn - (acknowledged-1) >= windowsize){
				System.out.print("");
				if(syn >= message.length()) break;
			}
			if(syn >= message.length()) break;
			//end send
		}
		TimeUnit.MILLISECONDS.sleep(100);
		while(terminateSend == false){
			try{
				//send
				baos = new ByteArrayOutputStream(65535);
			   	oos = new ObjectOutputStream(baos);
			   	sendTCP = new TCPPacket(0, 0, 0, 0, 0, 1, "");
			   	oos.writeObject(sendTCP);
			   	oos.flush();
			   	sendData = baos.toByteArray();
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9871);
				sendSocket.send(sendPacket);
				System.out.println("000000000000000000000000000000000000000000000000");
				//end send
			}catch(Exception e){}
			TimeUnit.MILLISECONDS.sleep(3000);
		}
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
			try{
				recTCP = (TCPPacket) o.readObject();
				Thread.sleep(2000);
				b_in.reset();
				System.out.println("Packet Received");
				if(recTCP.acknum == 0){
					terminateSend = true;
					System.out.println(terminateSend);
				}
				if(recTCP.finflag == 1){
					baos = new ByteArrayOutputStream(65535);
				   	oos = new ObjectOutputStream(baos);
				   	sendTCP = new TCPPacket(0, 0, 1, 0, 0, 0, "");
				   	oos.writeObject(sendTCP);
				   	oos.flush();
				   	sendData = baos.toByteArray();
					sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9871);
					sendSocket.send(sendPacket);
				}
				recTCP.printDetails();
				if(recTCP.acknum == acknowledged + 2) acknowledged = recTCP.acknum;
			}catch(Exception e){
				System.out.println("Error");
			}
		    }
		}.start();
	}
	public static void sendMessage(DatagramPacket p, TCPPacket t){
		final DatagramPacket sendPacket = p;
		final TCPPacket sendTCP = t;
		new Thread()
		{
			public void run() {
				while(acknowledged < sendTCP.synnum){
					if(acknowledged >= message.length()) System.out.println("Finished");
					try{
						sendSocket.send(sendPacket);
						Thread.sleep(4000);
					}catch(Exception e){
						System.out.println("Error");
					}
					if(terminateSend == true) break;
				}
			}
		}.start();
	}

}
