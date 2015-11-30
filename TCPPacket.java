import java.io.*;
public class TCPPacket implements Serializable{
	
	int synflag;
	int synnum;
	int ackflag;
	int acknum;
	int winsize;
	int finflag;
	String body;
	public TCPPacket(int synflag, int synnum, int ackflag, int acknum, int winsize, int finflag, String body){
		this.synflag = synflag;
		this.synnum = synnum;
		this.ackflag = ackflag;
		this.acknum = acknum;
		this.winsize = winsize;
		this.finflag = finflag;
		this.body = body;
	}
	
	public void printDetails(){
		System.out.println("synflag: " + this.synflag);
		System.out.println("synnum: " + this.synnum);
		System.out.println("ackflag: " + this.ackflag);
		System.out.println("acknum: " + this.acknum);
		System.out.println("winsize: " + this.winsize);
		System.out.println("finflag: " + this.finflag);
		System.out.println("body: " + this.body);
		System.out.println("End of Packet");
	}

}
