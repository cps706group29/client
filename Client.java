import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;

public class Client {
  public static String  HIS_CINEMA_IP;    // Default 127.0.0.1
  public static int     HIS_CINEMA_PORT;  // Default 40280
  public static String  LOCAL_DNS_IP;     // Default 127.0.0.1
  public static int     LOCAL_DNS_PORT;   // Default 40281
  public static int     HER_CDN_PORT;     // Default 40284

  public static void main(String[] args) throws IOException {
    // Set the IP/PORT constants
    initialize();

    // TCP Socket for communicatin with Web-Server hisCinema.com
    InetAddress ip = InetAddress.getByName(HIS_CINEMA_IP);
    Socket clientSocket = new Socket(ip, HIS_CINEMA_PORT);
    PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    System.out.println("Sending HTTP Request to hisCinema.com...");
    // -----------------------------------------------------------------------------
    pw.println("GET / HTTP/1.1");
    pw.println("Host: " + HIS_CINEMA_IP + ":" + HIS_CINEMA_PORT);
    pw.println("User-Agent: Phil, Ron and Jed's Client App");
    pw.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    pw.println("Accept-Language: en-US,en;q=0.5");
    pw.println("Accept-Encoding: gzip, deflate");
    pw.println("Connection: keep-alive");
    pw.println("Upgrade-Insecure-Requests: 1");
    pw.println("");
    // -----------------------------------------------------------------------------
    pw.flush();
    // Response Receieved - BUILD HTML RESPONSE FROM SERVER
    String message;
    String html = "";
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
    String resource = getResource(urlLinks.get(userSelection));
    System.out.println("REQUESTED RESOURCE IS: " + resource);
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
    System.out.println("SENDING TO LOCAL-DNS:    " + requestURL);
    udpSocket.send(sendPacket);

    // HANDLE THE RESPONSE FROM THE SERVER
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    udpSocket.receive(receivePacket);
    String HER_CDN_IP = new String(receivePacket.getData());
    System.out.println("RECEIVED FROM LOCAL-DNS: " + HER_CDN_IP);

    // TCP Socket for communicatin with Web-Server herCDN.com
    clientSocket = new Socket(InetAddress.getByName(HER_CDN_IP), HER_CDN_PORT);
    DataInputStream dataInput = new DataInputStream(clientSocket.getInputStream());
    PrintWriter dataOutput = new PrintWriter(clientSocket.getOutputStream());
    System.out.println("Sending HTTP Request to herCDN.com: " + HER_CDN_IP + ":" + HER_CDN_PORT + resource);
    // -----------------------------------------------------------------------------
    dataOutput.println("GET " + resource + " HTTP/1.1");
    dataOutput.println("Host: " + HER_CDN_IP + ":" + HER_CDN_PORT);
    dataOutput.println("User-Agent: Phil, Ron and Jed's Client App");
    dataOutput.println("Accept: video/mp4,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    dataOutput.println("Accept-Language: en-US,en;q=0.5");
    dataOutput.println("Accept-Encoding: gzip, deflate");
    dataOutput.println("Connection: keep-alive");
    dataOutput.println("Upgrade-Insecure-Requests: 1");
    dataOutput.println("");
    dataOutput.flush();
    // -----------------------------------------------------------------------------
    // Ready to receive video file
    try{
      String filename = dataInput.readUTF();
      long filesize = Long.parseLong(dataInput.readUTF());
      System.out.println("Receiving File: " + filename + " " + (filesize/(1024*1024)) + "MB" );
      receiveData = new byte[1024];
      // Open a new file in the current directly and to write the transfer
      File file = new File(System.getProperty("user.dir") + "/client-" + filename);
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      long bytesRead;
      do{
        bytesRead = dataInput.read(receiveData, 0, receiveData.length);
        fileOutputStream.write(receiveData, 0, receiveData.length);
      }while(!(bytesRead < 1024));
      fileOutputStream.close();
      System.out.println("Transfer complete.");
    }catch(EOFException e){
      System.out.println("ERROR: " + e);
    }
    dataInput.close();
    dataOutput.close();
    clientSocket.close();
    // ----------------------------------------------------------------------------------
    udpSocket.close();

    // /////////////////////////// JED's Work ////////////////////////////
    // AppFrame f = new AppFrame(urlLinks);
    // f.setVisible(true);

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

  private static String getResource(String url){
    Pattern p = Pattern.compile("([a-zA-Z0-9]+[.]{1}[A-Za-z0-9]+[.]{1}[A-Za-z0-9]+)([/]{1}[A-Za-z0-9]+[.]{1}[A-Za-z0-9]+)");
    Matcher m = p.matcher(url);
    while(m.find()){
      return m.group(2);
    }
    return "";
  }

  private static void initialize(){
    // INITIALIZES THE FOLLWING CONSTANTS
    // HIS_CINEMA_IP;    // Default 127.0.0.1
    // HIS_CINEMA_PORT;  // Default 40280
    // LOCAL_DNS_IP;     // Default 127.0.0.1
    // LOCAL_DNS_PORT;   // Default 40281
    // HER_CDN_PORT;     // Default 40284
    // Set the IP/PORT constants
    Scanner scanner = new Scanner(System.in);
    String line;
    // LOCAL_DNS_IP --------------------------------------------------------------------------
    System.out.println("Enter IP of Local DNS (or press 'Enter' for 127.0.0.1)");
    line = scanner.nextLine();
    if(line.isEmpty()){
      System.out.println("Using 127.0.0.1");
      line = "127.0.0.1";
    }
    while(!checkIP(line)){
      System.out.println("[Error] Invalid IP, try again!");
      System.out.println("Enter IP of Local DNS (or press 'Enter' for 127.0.0.1)");
      line = scanner.nextLine();
    }
    LOCAL_DNS_IP = line;
    // LOCAL_DNS_PORT --------------------------------------------------------------------------
    System.out.println("Enter PORT of Local DNS (or press 'Enter' for 40281)");
    line = scanner.nextLine();
    if(line.isEmpty()){
      System.out.println("Using 40281");
      line = "40281";
    }
    while(!checkPORT(line)){
      System.out.println("[Error] Invalid PORT, try again!");
      System.out.println("Enter PORT of Local DNS (or press 'Enter' for 40281)");
      line = scanner.nextLine();
    }
    LOCAL_DNS_PORT = Integer.parseInt(line);
    // HIS_CINEMA_IP --------------------------------------------------------------------------
    System.out.println("Enter IP of hisCinema.com Web Server (or press 'Enter' for 127.0.0.1)");
    line = scanner.nextLine();
    if(line.isEmpty()){
      System.out.println("Using 127.0.0.1");
      line = "127.0.0.1";
    }
    while(!checkIP(line)){
      System.out.println("[Error] Invalid IP, try again!");
      System.out.println("Enter IP of hisCinema.com Web Server (or press 'Enter' for 127.0.0.1)");
      line = scanner.nextLine();
    }
    HIS_CINEMA_IP = line;
    // HIS_CINEMA_PORT --------------------------------------------------------------------------
    System.out.println("Enter PORT of hisCinema.com Web Server (or press 'Enter' for 40280)");
    line = scanner.nextLine();
    if(line.isEmpty()){
      System.out.println("Using 40280");
      line = "40280";
    }
    while(!checkPORT(line)){
      System.out.println("[Error] Invalid PORT, try again!");
      System.out.println("Enter PORT of hisCinema.com Web Server (or press 'Enter' for 40280)");
      line = scanner.nextLine();
    }
    HIS_CINEMA_PORT = Integer.parseInt(line);
    // HER_CDN_PORT --------------------------------------------------------------------------
    System.out.println("Enter PORT of HerCDN (or press 'Enter' for 40284)");
    line = scanner.nextLine();
    if(line.isEmpty()){
      System.out.println("Using 40284");
      line = "40284";
    }
    while(!checkPORT(line)){
      System.out.println("[Error] Invalid PORT, try again!");
      System.out.println("Enter PORT of HerCDN (or press 'Enter' for 40284)");
      line = scanner.nextLine();
    }
    HER_CDN_PORT = Integer.parseInt(line);
    // --------------------------------------------------------------------------
    System.out.println("[LocalDNS]  " + LOCAL_DNS_IP + ":" + LOCAL_DNS_PORT);
    System.out.println("[HisCinema] " + HIS_CINEMA_IP + ":" + HIS_CINEMA_PORT);
    System.out.println("[HerCDN]    xxx.xxx.xxx.xxx:" + HER_CDN_PORT);
    return;
  }

  private static boolean checkIP(String input){
    Pattern p = Pattern.compile("([0-9]+[.]){3}[0-9]{1}");
    Matcher m = p.matcher(input);
    if(m.find()){
      return true;
    }
    return false;
  }

  private static boolean checkPORT(String input){
    Pattern p = Pattern.compile("[0-9]+");
    Matcher m = p.matcher(input);
    if(m.find()){
      return true;
    }
    return false;
  }

}
