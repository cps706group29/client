import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;


public class Client {

  public static void main(String[] args) throws IOException {
    String inputFilename;
    String html = "";

    InetAddress ip = InetAddress.getByName("127.0.0.1");
    Socket clientSocket = new Socket(ip, 40280);

    PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    System.out.println("Connecting to web server...");
    System.out.println("");
    
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

    String message;
    while((message = inFromServer.readLine()) != null){
       html += message + '\n';
    }
    
    System.out.println("------------- RECEIVED HTML -------------");
    System.out.println(html);
    System.out.println("----------------------------------------" + '\n');
    
    ArrayList urlLinks = parseHTML(html);

    /**if (!urlLinks.isEmpty()){
      System.out.println("We have found " + urlLinks.size() + " links in the page.");
      System.out.println("Please enter the number of the link you want to visit: ");
      for (int i=0; i < urlLinks.size(); i++){
        System.out.println("   " + i + " = " + urlLinks.get(i));
      }
    }*/
    
    pw.close();
    inFromServer.close();

    clientSocket.close();
    
    AppFrame f = new AppFrame(urlLinks);
    f.setVisible(true);
    
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
}
