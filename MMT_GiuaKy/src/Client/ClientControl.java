package Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

public class ClientControl extends JFrame implements ActionListener {
    /*-----------------------------------------------------------------------------------------------------*/
    private JButton connectButton;
    private JButton shutdownButton;
    private JButton exitButton;
    private JTextField severIP_text;
    private JPanel ClientControl;
    private JLabel severIP_label;
    private JTextField Status;
    private JPanel IP_frame;
    private JTabbedPane Request_frame;
    private JButton takePicButton;
    private JButton zoomPicButton;
    private JButton savePicButton;
    private JPanel Image;
    private JButton showProcessButton;
    private JButton startProcessButton;
    private JButton killProcessButton;
    private JButton clearProcessButton;
    private JButton showAppButton;
    private JButton startAppButton;
    private JButton clearAppButton;
    private JButton killAppButton;
    private JButton hookButton;
    private JButton clearKeyButton;
    private JButton unHookButton;
    private JPanel KeyShow;
    private JButton disconnectButton;
    private JTable ApplicationTable;
    private JTable ProcessTable;
    private JTextField shutdownTime;
    private JLabel imageResult;
    private JTextArea showKeyText;
    private JScrollPane listProcess;
    private JScrollPane listApp;
    private JTextArea testText;
    private JButton printKeyButton;

    /*-----------------------------------------------------------------------------------------------------*/
    public static PrintWriter outToServer;
    public static BufferedReader inFromServer;
    public static Socket clientSocket;
    public static BufferedImage img;
    public static ObjectInputStream oin;
    public static byte[] bytes;


    /*-----------------------------------------------------------------------------------------------------*/

    public ClientControl()
    {
        setContentPane(ClientControl);
        setTitle("Client Controller");
        setSize(750,500);
        setBounds(250,150,750,500);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
//        setLocationRelativeTo((Component) null);
        getContentPane().setBackground(Color.CYAN);

        exitButton.addActionListener(this);
        connectButton.addActionListener(this);
        disconnectButton.addActionListener(this);

        takePicButton.addActionListener(this);
        savePicButton.addActionListener(this);
        zoomPicButton.addActionListener(this);

        showAppButton.addActionListener(this);
        clearAppButton.addActionListener(this);
        killAppButton.addActionListener(this);
        startAppButton.addActionListener(this);

        showProcessButton.addActionListener(this);
        clearProcessButton.addActionListener(this);
        killProcessButton.addActionListener(this);
        startProcessButton.addActionListener(this);

        hookButton.addActionListener(this);
        unHookButton.addActionListener(this);
        printKeyButton.addActionListener(this);
        clearKeyButton.addActionListener(this);

        shutdownButton.addActionListener(this);

        Status.setText("DISCONNECTED");
        Status.setForeground(Color.RED);
        severIP_text.addActionListener(this);
        shutdownTime.addActionListener(this);

        clearAppTable();
        clearProcessTable();
    }

    public void clearAppTable() {
        List<String[]> maindata = new ArrayList<>();
        String[] data = {"", ""};
        maindata.add(data);
        AppTableModel table = new AppTableModel(maindata);
        ApplicationTable.setModel(table);
    }
    public void clearProcessTable() {
        List<String[]> maindata = new ArrayList<>();
        String[] data = {"", "", "", "", ""};
        maindata.add(data);
        ProcessTableModel table = new ProcessTableModel(maindata);
        ProcessTable.setModel(table);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == exitButton) System.exit(0);
        else if(e.getSource() == connectButton)
        {
            String serverIP = severIP_text.getText();
            boolean isConnected = false;
            clientSocket = null;

            if(serverIP.equals(""))
            {
                JOptionPane.showMessageDialog(ClientControl,"Connecting Error! Server IP do not blank!");
            }
            else
            {
                System.out.println("Connecting to " + serverIP + "...");

                try {
                    clientSocket = new Socket(serverIP, 6789);
                    isConnected = true;
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(ClientControl,"Connecting Error!");
                    clientSocket = null;
                }

                if(isConnected)
                {
                    Status.setText("CONNECTED");
                    Status.setForeground(Color.GREEN);
                    JOptionPane.showMessageDialog(ClientControl,"Connect Successfully!");

                    try {
                        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        outToServer = new PrintWriter(clientSocket.getOutputStream());
                    } catch (IOException ex) {
                        Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        else if(e.getSource() == disconnectButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                Status.setText("DISCONNECTED");
                Status.setForeground(Color.RED);
                outToServer.println("DISCONNECTED");
                outToServer.flush();

                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                }
                clientSocket = null;
            }
        }
        else if(e.getSource() == shutdownButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                if(shutdownTime.getText().equals("")) JOptionPane.showMessageDialog(ClientControl,"Please input shutdown time!");
                else
                {
                    String s = "SHUTDOWN";
                    outToServer.println(s);
                    outToServer.flush();
                    outToServer.println(shutdownTime.getText());
                    outToServer.flush();
                }
            }
        }

        else if(e.getSource() == showAppButton)        //done
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                clearAppTable();
                String process_from_server = null;
                int last = 0;
                int size = 0;
                String[] temp;
                String s = "SHOW_APPLICATION";
                outToServer.println(s);
                outToServer.flush();

                try {
                    size = Integer.parseInt(inFromServer.readLine());
                } catch (IOException ex) {
                    Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                }

                List<String[]> maindata = new ArrayList<>();

                for (int i = 0; i < size; i++){
                    try {
                        process_from_server = inFromServer.readLine().trim();
                    } catch (IOException ex) {
                        System.out.println("Lỗi");
                        Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if(process_from_server == null) continue;

                    temp = process_from_server.split(" ");

                    for (String w : temp) last++;

                    String[] data = new String[2];
                    data[0] = "";       data[1] = temp[last-1];

                    for (int j = 0; j < last-1; j++)
                        data[0] = data[0] + temp[j]+ " ";

//                    for (int j =0; j<2; j++)
//                        System.out.print(data[j] + " ");
//                    System.out.println();

                    maindata.add(data);
                    last = 0;
                }

                AppTableModel table = new AppTableModel(maindata);
                ApplicationTable.setModel(table);
            }
        }
        else if(e.getSource() == startAppButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                String res = JOptionPane.showInputDialog("Nhập tên ứng dụng cần mở (ứng dụng cơ bản): ");
                String s = "START";
                outToServer.println(s);
                outToServer.flush();
                outToServer.println(res);
                outToServer.flush();
            }
        }
        else if(e.getSource() == killAppButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                String res = JOptionPane.showInputDialog("Nhập PID cần hủy: ");
                String s = "KILL_PID";
                outToServer.println(s);
                outToServer.flush();
                outToServer.println(res);
                outToServer.flush();
            }
        }
        else if(e.getSource() == clearAppButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else clearAppTable();
        }

        else if(e.getSource() == showProcessButton)  //done
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                clearProcessTable();

                String process_from_server = null;
                int last = 0;
                int size = 0;
                String[] temp;
                String s = "SHOW_PROCESS";
                outToServer.println(s);
                outToServer.flush();

                try {
                    size = Integer.parseInt(inFromServer.readLine());
                } catch (IOException ex) {
                    Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                }

                List<String[]> maindata = new ArrayList<>();

                for (int i = 0; i< size-1; i++){
                    try {
                        process_from_server = inFromServer.readLine().trim();
                    } catch (IOException ex) {
                        System.out.println("Loi~");
                        Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if(process_from_server == null) continue;

                    temp = process_from_server.split(" ");

                    for (String w : temp) last++;

                    String[] data = new String[5];
                    data[4] = temp[last-2]+" K";
                    data[3] = temp[last-3];
                    data[2] = temp[last-4];
                    data[1] = temp[last-5];
                    data[0] = "";

                    for (int j =0; j<last-5; j++)
                        data[0] = data[0] + temp[j]+ " ";

//                    for (int j =0; j<5; j++)
//                        System.out.print(data[j] + " ");
//                    System.out.println();

                    maindata.add(data);
                    last = 0;
                }
                ProcessTableModel table = new ProcessTableModel(maindata);
                ProcessTable.setModel(table);
            }
        }
        else if(e.getSource() == startProcessButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                String res = JOptionPane.showInputDialog("Nhập tên ứng dụng cần mở (ứng dụng cơ bản): ");
                String s = "START";
                outToServer.println(s);
                outToServer.flush();
                outToServer.println(res);
                outToServer.flush();
            }
        }
        else if(e.getSource() == killProcessButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                String res = JOptionPane.showInputDialog("Nhập PID cần hủy: ");
                String s = "KILL_PID";
                outToServer.println(s);
                outToServer.flush();
                outToServer.println(res);
                outToServer.flush();
            }
        }
        else if(e.getSource() == clearProcessButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else clearProcessTable();
        }

        else if(e.getSource() == takePicButton) //done
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                String s = "TAKE";
                img = null;
                outToServer.println(s);
                outToServer.flush();

                try {
                    oin = new ObjectInputStream(clientSocket.getInputStream());

                    try {
                        bytes = (byte[]) oin.readObject();
                    } catch (IOException | ClassNotFoundException ex) {
                        Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    InputStream in = new ByteArrayInputStream(bytes);

                    img = ImageIO.read(in);
                    Image img1 = img;
                    ImageIcon imgIcon = new ImageIcon(img1.getScaledInstance(Image.getWidth(), Image.getHeight(), 4));

                    imageResult.setIcon(imgIcon);

                } catch (IOException ex) {
                    Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else if(e.getSource() == savePicButton) //done
        {
            if (clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                if (img == null) JOptionPane.showMessageDialog(rootPane, "Không có ảnh nào");
                else
                {
                    String path = "E://Java//Screenshot//tmp.png";
                    try {
                        ImageIO.write(img , "png", new File(path));
                        JOptionPane.showMessageDialog(rootPane, "Đã lưu thành công vào E://Java//Screenshot");
                    } catch (IOException ex) {
                        Logger.getLogger(ClientControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        else if(e.getSource() == zoomPicButton) //done
        {
            if (clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                if (img == null) JOptionPane.showMessageDialog(rootPane, "Không có ảnh nào");
                else
                {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    Image imgtmp = img;
                    ImageIcon icon2 = new ImageIcon(imgtmp.getScaledInstance((int) screenSize.getWidth()-100, (int) screenSize.getHeight()-200, 4));
                    JOptionPane.showMessageDialog(null, icon2, "Screenshot", JOptionPane.PLAIN_MESSAGE, null);
                }
            }
        }

        else if(e.getSource() == hookButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                String s = "HOOK";
                outToServer.println(s);
                outToServer.flush();
//                s = "";
//
//                while (true)
//                {
//                    try {
//                        String key = inFromServer.readLine();
//
//                        if(key.length() > 1) System.out.println(key);
//                        else System.out.print(key);
//
//                        s = s + key;
//
//                        showKeyText.setText(s);
//
//                    } catch (IOException ex) {
//                        break;
////                        throw new RuntimeException(ex);
//                    }
//                }
            }
        }
        else if(e.getSource() == unHookButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                String s = "UNHOOK";
                outToServer.println(s);
                outToServer.flush();
            }
        }
        else if(e.getSource() == clearKeyButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                showKeyText.setText("");
                testText.setText("");

                String s = "CLEARKEY";
                outToServer.println(s);
                outToServer.flush();
            }
        }
        else if(e.getSource() == printKeyButton)
        {
            if(clientSocket == null) JOptionPane.showMessageDialog(ClientControl,"You didn't connect to server!");
            else
            {
                String s = "PRINT";
                outToServer.println(s);
                outToServer.flush();

                try {
                    String text = inFromServer.readLine();

                    System.out.println("hookString: " + text + " end");
                    showKeyText.setText(text);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }


    public static void main(String[] args) { new ClientControl(); }
}
