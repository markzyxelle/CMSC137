import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
public class MiniServerSocketExample {
    private static final int PORT = 9876;
    public static void main(String[] args) {
    	try {
        	ServerSocket server = new ServerSocket(PORT);
            	Socket insocket = server.accept();
            	InputStream is = insocket.getInputStream();
            	PrintWriter out = new PrintWriter(insocket.getOutputStream());
        	BufferedReader in = new BufferedReader(new InputStreamReader(is));
            	String line;
            	line = in.readLine();
            	String request_method = line;
            	//System.out.println("HTTP-HEADER1: " + line);
            	line = "";
            	// looks for post data
            	out.println("HTTP/1.0 200 OK");
		out.println("Content-Type: text/html; charset=utf-8");
		out.println("Server: MINISERVER");
		// this blank line signals the end of the headers
		out.println("");
		// Send the HTML page
		out.println("<H1>PORT: " + PORT + "</H1>");
		out.println("<H2>" + request_method + "</H2>");
		String file = "";
            	if(request_method.contains("POST")){
            		file = request_method.split(" ")[1];
		    	int postDataI = -1;
		    	while ((line = in.readLine()) != null && (line.length() != 0)) {
		        	if (line.indexOf("Content-Length:") > -1) {	
		         	   	postDataI = new Integer(line.substring(line.indexOf("Content-Length:") + 16,line.length())).intValue();
		        	}
		    	}
		    	String postData = "";
		    	// read the post data
		    	if (postDataI > 0) {
		        	char[] charArray = new char[postDataI];
		        	in.read(charArray, 0, postDataI);
		        	postData = new String(charArray);
		    	}
		    	try{
		    		out.println("<table>");
		    		String temp = "";
			    	Scanner scanner = new Scanner(postData);
				while (scanner.hasNextLine()) {
					temp = scanner.nextLine();
					if(temp.contains("\"")){
						out.println("<tr><td>" + temp.split("\"")[1] + "</td>");
					}
					if(temp.trim().equals("")) out.println("<td>" + scanner.nextLine() + "</td></tr>");	
				}
				scanner.close();
				out.println("</table>");
			}catch(Exception e){}
		}
		else if(request_method.contains("GET")){
			out.println("<table>");
			String ext = request_method.split(" ")[1];
			if(ext.contains("?")){	
				file = ext.split("\\?")[0];
				String data = ext.split("\\?")[1];
				if(data.contains("&")){
					String[] pairs = data.split("&");
					for(String q : pairs){
						out.println("<tr>");
						out.println("<td>" + q.split("=")[0] + "</td>");
						out.println("<td>" + q.split("=")[1] + "</td>");
						out.println("</tr>");
					}
				}
				else{
					out.println("<tr>");
					out.println("<td>" + data.split("=")[0] + "</td>");
					out.println("<td>" + data.split("=")[1] + "</td>");
					out.println("</tr>");
				}	
			}
			out.println("<table>");
		}
            	//System.out.println("<H2>" + postData + "</H2>");
            	//out.println("<form name=\"input\" action=\"form_submited\" method=\"post\">");
            	//out.println("Username: <input type=\"text\" name=\"user\"><input type=\"submit\" value=\"Submit\"></form>");
            	out.close();
            	insocket.close();
            	try{
            		File origfile = new File(file);
    	    		File newfile = new File(file.split("/")[file.split("/").length-1]);
    	    		
    	    		InputStream copyStream = new FileInputStream(origfile);
		    	OutputStream pasteStream = new FileOutputStream(newfile);
			
		    	byte[] buffer = new byte[1024];
		    		
		    	int length;
		    	while ((length = copyStream.read(buffer)) > 0){
		    	  
		    		pasteStream.write(buffer, 0, length);
		    	 
		    	}
		    	 
		    	copyStream.close();
		    	pasteStream.close();
		    	    
		    	}catch(IOException e){
		    		e.printStackTrace();
		    	}
        } catch (Exception e) {
        }
    }
}
