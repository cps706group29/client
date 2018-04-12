import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;


public class Client {
  public static final String  HIS_CINEMA_IP =   "127.0.0.1";
  public static final int     HIS_CINEMA_PORT = 40280;
  public static final String  LOCAL_DNS_IP =    "127.0.0.1";
  public static final int     LOCAL_DNS_PORT =  40281;


  public static void main(String[] args) throws IOException {
    String inputFilename;
    String html = "";

    // TCP Socket for communicatin with Web-Server hisCinema.com
    InetAddress ip = InetAddress.getByName(HIS_CINEMA_IP);
    Socket clientSocket = new Socket(ip, HIS_CINEMA_PORT);

    PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    System.out.println("Sending HTTP Request to hisCinema.com...");
    System.out.println("---------------------------------------------------------------------");
    pw.println("GET / HTTP/1.1");
    pw.println("Host: 127.0.0.1:40280");
    pw.println("User-Agent: Phil, Ron and Jed's Client App");
    pw.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    pw.println("Accept-Language: en-US,en;q=0.5");
    pw.println("Accept-Encoding: gzip, deflate");
    pw.println("Connection: keep-alive");
    pw.println("Upgrade-Insecure-Requests: 1");
    pw.println("");
    pw.flush();
    System.out.println("---------------------------------------------------------------------");
    // Response Receieved - BUILD HTML RESPONSE FROM SERVER
    String message;
    while((message = inFromServer.readLine()) != null){
       html += message + '\n';
    }
    System.out.println("------------- RECEIVED HTML -------------");
    System.out.println(html);
    System.out.println("----------------------------------------" + '\n');
    pw.close();
    // Close TCP socket/connection
    inFromServer.close();
    clientSocket.close();

    // Build URL list from the HTML response
    ArrayList<String> urlLinks = parseHTML(html);
    if (!urlLinks.isEmpty()){
      System.out.println("We have found " + urlLinks.size() + " links in the page.");
      System.out.println("Please enter the number of the link you want to visit: ");
      for (int i=0; i < urlLinks.size(); i++){
        System.out.println("   " + i + " = " + urlLinks.get(i));
      }
    }
    // Get user video selection
    BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
    int userSelection = Integer.parseInt(userInput.readLine());
    while(userSelection >= urlLinks.size() || userSelection < 0){
      System.out.println("Please enter a number between 0 and " + (urlLinks.size()-1));
      userSelection = Integer.parseInt(userInput.readLine());
    }
    String domain = getDomain(urlLinks.get(userSelection));
    // Prepare UDP Packet to send to localDNS to resolve domain name for video
    DatagramSocket udpSocket = new DatagramSocket();
    InetAddress IPAddress = InetAddress.getByName(LOCAL_DNS_IP);
    byte[] sendData = new byte[1024];
    byte[] receiveData = new byte[1024];
    // The request should be a DNS query
    String requestURL = domain;
    sendData = requestURL.getBytes();
    // Send the DNS Query to the server, and await response
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, LOCAL_DNS_PORT);
    System.out.println("TO SERVER: " + requestURL);
    udpSocket.send(sendPacket);

    // HANDLE THE RESPONSE FROM THE SERVER
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    udpSocket.receive(receivePacket);
    String serverResponse = new String(receivePacket.getData());
    System.out.println("FROM SERVER: " + serverResponse);
    udpSocket.close();






    // /////////////////////////// JED's Work ////////////////////////////
    // AppFrame f = new AppFrame(urlLinks);
    // f.setVisible(true);

  }

  public static void printH(String test){
    System.out.println(test);
  }


  private static ArrayList<String> parseHTML(String html){

    ArrayList<String> links = new ArrayList<String>();

    Pattern p = Pattern.compile("<a[^>]+href=[\"']?([^>\"']*)[\"']?[^>]*>(.+?)</a>");
    Matcher m = p.matcher(html);
    while (m.find()) {
      links.add(m.group(1));
    }
    return links;
  }

  private static String getDomain(String url){
    Pattern p = Pattern.compile("([a-zA-Z0-9]+[.]{1}[A-Za-z0-9]+[.]{1}[A-Za-z0-9]+)([/]{1}[A-Za-z0-9]+)");
    Matcher m = p.matcher(url);
    while(m.find()){
      return m.group(1);
    }
    return "";
  }


}
