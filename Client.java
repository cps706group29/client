import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Client {

  public static void main(String[] args) throws IOException {
    String inputFilename;

    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    InetAddress ip = InetAddress.getByName("127.0.0.1");
    Socket clientSocket = new Socket(ip, 40280);

    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());

    // System.out.print("Please enter the file name that you want to request from the server");
    // System.out.println("(or press \'Enter\'/blank for index.html): ");
    // inputFilename = inFromUser.readLine();
    // if(inputFilename.isEmpty()){
    //   System.out.println("TRUE");
    //   inputFilename = " ";
    // }

    inputFilename = "index.html";
    if (!inputFilename.isEmpty()){
      System.out.println("GET /" + inputFilename + " HTTP/1.1");
      pw.println("GET /" + inputFilename + " HTTP/1.1");
      pw.println("Host: 127.0.0.1:40280");
      pw.println("User-Agent: Phil, Ron and Jed's Client App");
      pw.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
      pw.println("Accept-Language: en-US,en;q=0.5");
      pw.println("Accept-Encoding: gzip, deflate");
      pw.println("Connection: keep-alive");
      pw.println("Upgrade-Insecure-Requests: 1");
      pw.println("");


      pw.flush();

      String t;
      while((t = inFromServer.readLine()) != null){
        System.out.println(t);
      }

      pw.close();
      inFromServer.close();

      clientSocket.close();


    }
  }

  private static ArrayList<String> parseHTML(String html){
    ArrayList<String> links = new ArrayList<String>();
    String regex = "(href=\"){1}[A-Za-z0-9\.\/]+(\"){1}";
    
    return links;
  }
}
