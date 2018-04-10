package cps706project;
import java.io.*;
import java.net.*;



public class Client {
  
  public static void main(String[] args) throws IOException {
    String sentence;
    String modifiedSentence;
    
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    InetAddress ip = InetAddress.getByName("192.168.1.116");
    Socket clientSocket = new Socket(ip, 40280);
    
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
    
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    
    sentence = inFromUser.readLine();
    
    outToServer.writeBytes(sentence + '\n');
    
    modifiedSentence = inFromServer.readLine();
    
    System.out.println("SERVER: " + modifiedSentence);
    
    clientSocket.close();
    
  }
  
}
