/*
 * programmed by mofselvi
 */
package mofmarketprocessor;

/**
 *
 * @author mofselvi
 */
import java.io.*;
import java.net.*;

class SocketListener implements Runnable
{
    public String msgIN = "";
    public String msgOUT = "";
    String msgFromClient;
    String msgToClient;
    ServerSocket welcomeSocket;

    public SocketListener() throws Exception
    {

    }
    
    public synchronized void SetMsgIN(String newmsg)
    {
        msgIN = newmsg;
    }
    public synchronized String GetMsgIN()
    {
        return msgIN;
    }
    public synchronized void SetMsgOUT(String newmsg)
    {
        msgOUT = newmsg;
    }
    public synchronized String GetMsgOUT()
    {
        return msgOUT;
    }
    
    public void MsgLoop()
    {
        try
        {
            while (true)
            {
                synchronized(msgIN)
                {
                    welcomeSocket = new ServerSocket(5858);
                    Socket connectionSocket = welcomeSocket.accept();
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                    msgFromClient = inFromClient.readLine();
                    System.out.println("Received: " + msgFromClient);
                    
                    msgToClient = "MMP Response: "+msgFromClient.toUpperCase() + '\n';
                    outToClient.writeBytes(msgToClient);

                    
                    inFromClient.close();
                    outToClient.close();
                    connectionSocket.close();
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Err");
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        MsgLoop();
    }
}
