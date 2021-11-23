package mofmarketprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

/**
 *
 * @author mofselvi
 */
public class MofMarketProcessor
{

    public static void main(String[] args)
    {
        System.out.println("Mof Market Processor v2.13");
        System.out.println("Initializing...");
        
        String var_localserver = "https://bitrogen.com";
        String var_dbhost = "localhost";
        String var_dbuser = "dbusr_bankanat";
        String var_dbpass = "BSN9gnDPjYbBinxWlUKP";
        String var_dbname = "anatolia_bankanatoliandb";
        
        //fetch process save/send
        Controller ctrl = new Controller(var_localserver, var_dbhost, var_dbuser, var_dbpass, var_dbname);
        Thread thread = new Thread(ctrl);
        thread.start();
        
        //check
        /*Controller2 ctrl2 = new Controller2(var_localserver);
        Thread thread2 = new Thread(ctrl2);
        thread2.start();*/
        
        /*String chkjob = HTTPConn(var_localserver+"/bao_job.go?pass=1071asmincronjob", "GET", "");
        System.out.println(""+chkjob);*/
        
        //Scanner scanner = new Scanner(System.in);
        String msgIN = "";
        String msgOUT = "";
        String msgFromClient;
        String msgToClient;
        ServerSocket welcomeSocket;
    
        
        try
        {
            welcomeSocket = new ServerSocket(5858);
            System.out.println("Initialized.");
            
            while(true)
            {
                System.out.println("Mof Market Processor Command:");
                //String newcmd = scanner.nextLine();

                //create & read socket
                Socket connectionSocket = welcomeSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                msgFromClient = inFromClient.readLine();
                System.out.println("Received: " + msgFromClient);
                
                String newcmd = msgFromClient;

                if(newcmd.equals("exit") || newcmd.equals("quit"))
                {
                    if(ctrl.GetActive())
                    {
                        System.out.println("Warning! Server is still running.");
                    }
                    else
                    {
                        ctrl.SetCommand(newcmd);
                        //ctrl2.SetCommand(newcmd);
                        System.out.println("Shutting the server down...");
                        
                        try
                        {
                            Thread.sleep(3000);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        msgToClient = ctrl.GetResponse();
                        if(!msgToClient.isEmpty())
                        {
                            outToClient.writeBytes(msgToClient);
                            //close connection
                            inFromClient.close();
                            outToClient.close();
                            connectionSocket.close();
                        }
            
                        break;
                    }
                }
                else
                {
                    ctrl.SetCommand(newcmd);
                }
                
                
                try
                {
                    Thread.sleep(1000);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                //send response to client
                msgToClient = ctrl.GetResponse();
                outToClient.writeBytes(msgToClient);
                
                //close connection
                inFromClient.close();
                outToClient.close();
                connectionSocket.close();
                
            }
            
            
            
            welcomeSocket.close();
        }
        catch(Exception e)
        {
            ctrl.SetCommand("stop");
            System.out.println("Exc");
            e.printStackTrace();
        }


        while(ctrl.GetActive())// || ctrl2.GetActive()
        {
            System.out.println("Waiting threads to stop!");
            
            try
            {
                Thread.sleep(5000);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static String HTTPConn(String link, String method, String params)
    {
        String rt = "";
        try
        {
            URL serverUrl = new URL(link);
            HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();
            
            if(method.isEmpty())
            {
                method = "GET";
            }
            urlConnection.setRequestMethod(method);
            
            if(!params.isEmpty())
            {
                urlConnection.setDoOutput(true);
                BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                httpRequestBodyWriter.write(params);//"visitorName=Johnny+Jacobs&luckyNumber=1234"
                httpRequestBodyWriter.close();
            }
            
            Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
            while(httpResponseScanner.hasNextLine())
            {
                rt += httpResponseScanner.nextLine();
            }
            httpResponseScanner.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return rt;
    }
}
