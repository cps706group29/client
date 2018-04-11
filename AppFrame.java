import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.util.*;

class AppFrame extends JFrame {

  private JButton btnGo  = new JButton("Go");

  private JLabel lblA = new JLabel("Links found in the webserver page: ");
  
  private JList urlList = new JList();
  
  private ArrayList links = new ArrayList(); 
 
  public AppFrame(ArrayList list){
    setTitle("Client");
    setSize(600,200);
    setLocationRelativeTo(null);
    setLayout(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);

    links = list;
    String[] stringArray = Arrays.copyOf(links.toArray(), links.toArray().length, String[].class);
    initComponent(stringArray);    
    initEvent();    
  }

  private void initComponent(String[] list){
    if (list.length > 0 ){
      urlList = new JList(list);
    }
    
    btnGo.setBounds(490,120,80,25);

    
    lblA.setBounds(10,0,250,50);
    urlList.setBounds(10,40,450,105);

    add(btnGo);
    add(urlList);
    add(lblA);

  }

  private void initEvent(){
    btnGo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Client.printH((String) urlList.getSelectedValue());
      }
    });
  }
}