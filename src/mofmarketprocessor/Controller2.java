/*
 * programmed by mofselvi
 */

package mofmarketprocessor;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author mofselvi
 */
public class Controller2 implements Runnable
{
    
    private final String localserver;
    
    public String command = "";
    public boolean active = false;
    
    
    public synchronized void SetCommand(String newcommand)
    {
        command = newcommand;
    }
    public synchronized String GetCommand()
    {
        return command;
    }
    public synchronized void SetActive(boolean newactive)
    {
        active = newactive;
    }
    public synchronized boolean GetActive()
    {
        return active;
    }
    
    
    public Controller2(String localserver_)
    {
        this.localserver = localserver_;
    }
    
    @Override
    public void run()
    {
        while(true)
        {
            synchronized(command)
            {
                if(!command.isEmpty())
                {
                    if(command.equals("exit") || command.equals("quit"))
                    {
                        Date date = new Date();
                        System.out.println(" #Quiting wallet checker... @"+date);
                        SetActive(false);
                        break;
                    }
                }
            }
            
            
            if(GetActive())
            {
                Control2();
            }
        }
    }
    
    public void Control2()
    {
        try
        {
            Thread.sleep(1000*60);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        //External GET - check
        HTTPConn(localserver+"/job_check.php", "GET", "");
    }
    
    public String HTTPConn(String link, String method, String params)
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