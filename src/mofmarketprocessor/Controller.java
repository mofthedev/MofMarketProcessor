/*
 * programmed by mofselvi
 */

package mofmarketprocessor;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author mofselvi
 */
public class Controller implements Runnable
{
    public String command = "";
    public String response = "";
    public boolean active = false;
    public long lastunixtime;
    public long endingtime;
    
    private final String localserver;
    private final String db_host;
    private final String db_user;
    private final String db_pass;
    private final String db_name;
    
    
    private DB db = null;
    private Processor processor = null;
    
    public Controller(String localserver_, String db_host_, String db_user_, String db_pass_, String db_name_)
    {
        this.localserver = localserver_;
        this.db_host = db_host_;
        this.db_user = db_user_;
        this.db_pass = db_pass_;
        this.db_name = db_name_;
    }

    public void Print(String s)
    {
        s = s + '\n';
        System.out.print(s);
        SetResponse(s);
    }
    public synchronized void SetResponse(String newresponse)
    {
        if(!newresponse.isEmpty())
        {
            String oldResponse = ReadResponse();
            if(!oldResponse.isEmpty())
            {
                oldResponse = oldResponse.replaceAll("\n", "|");
            }
            response = oldResponse+"|"+newresponse;
        }
        else
        {
            response = newresponse;
        }
    }
    public synchronized String GetResponse()
    {
        String rt = response;
        SetResponse("");
        return rt;
    }
    public synchronized String ReadResponse()
    {
        String rt = response;
        return rt;
    }
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
        if(active)
        {
            Start();
        }
        else
        {
            Stop();
        }
    }
    public synchronized boolean GetActive()
    {
        return active;
    }
    
    private void Start()
    {
        if(db == null)
        {
            db = new DB(db_host, db_user, db_pass, db_name);
        }
        if(processor == null)
        {
            processor = new Processor();
        }
        DBLoadInit();
    }
    private void Stop()
    {
        DBSave();
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
                    if(command.equals("stop"))
                    {
                        Date date = new Date();
                        Print(" #Stopping server... @"+date.toString());
                        SetActive(false);
                        //active = false;
                        //break;
                    }
                    else if(command.equals("start"))
                    {
                        Date date = new Date();
                        Print(" #Starting server... @"+date);
                        SetActive(true);
                        //active = true;
                    }
                    else if(command.equals("exit") || command.equals("quit"))
                    {
                        Date date = new Date();
                        Print(" #Quiting server... @"+date);
                        SetActive(false);
                        break;
                        //active = true;
                    }
                    else if(command.equals("isalive"))
                    {
                        Date date = new Date();
                        Print(" #MMP Server is alive... @"+date);
                    }
                    else if(command.equals("check"))
                    {
                        Date date = new Date();
                        if(GetActive())
                        {
                            Print(" #MMP Server is active... @"+date);
                        }
                        else
                        {
                            Print(" #MMP Server is NOT active... @"+date);
                        }
                    }

                    //command = "";
                    SetCommand("");
                }
            }
            
            
            if(GetActive())
            {
                Control();
            }
        }
        
        endingtime = UnixTime()+3;
        long newunixtime = UnixTime();
        while(true)
        {
            newunixtime = UnixTime();
            if(newunixtime >= endingtime)
            {
                break;
            }
            if(lastunixtime!=newunixtime)
            {
                lastunixtime = newunixtime;
                Print(" # Quiting in "+(endingtime-lastunixtime));
            }
        }
        
        Print(" # - END -");
    }
    
    public void Control()
    {
        
        //External GET - fetch
        //HTTPConn(localserver+"/job_fetch.php", "GET", "");
        
        //External GET - check
        //HTTPConn(localserver+"/job_check.php", "GET", "");
        
        //External GET - baojob
        HTTPConn(localserver+"/bao_job.go?pass=1071asmincronjob", "GET", "");
        
        DBLoadChange();
        processor.ProcessOrders();
        DBSave();
        
        //External GET - send
        //HTTPConn(localserver+"/job_send.php", "GET", "");
        
        try
        {
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void DBLoadInit()
    {
        if(db==null)
        {
            return;
        }
        db.DB_Connect();
        
        
        ArrayList<User> usrs = db.DB_Fetch_Users('1');
        processor.LoadUsers(usrs);
        
        ArrayList<Order> ordrs = db.DB_Fetch_Orders('1');
        processor.LoadOrders(ordrs);
        
        
        db.DB_Close();
    }
    
    public void DBLoadChange()
    {
        if(db==null)
        {
            return;
        }
        db.DB_Connect();
        
        
        processor.users_p = db.DB_Fetch_Users('0');//p
        
        processor.orders_p = db.DB_Fetch_Orders('0');//p
        
        processor.orders_d = db.DB_Fetch_Orders('d');//r
        
        
        db.DB_Close();
    }
    
    public void DBSave()
    {
        if(db==null)
        {
            return;
        }
        db.DB_Connect();
        
        
        db.DB_Apply_Case(processor.casex);
        processor.casex.ZeroAll();
        
        
        for (User user : processor.users_changed)
        {
            user.status = '1';
        }
        db.DB_Apply_Users(processor.users_changed);
        processor.users_changed.clear();
        
        
        for (Order orderp : processor.orders_changed)
        {
            orderp.status = '1';
        }
        db.DB_Apply_Orders(processor.orders_changed);
        processor.orders_changed.clear();
        
        
        for (Order orderd : processor.orders_deleted)
        {
            if(orderd != null)
            {
                orderd.status = 'x';//d
            }
        }
        db.DB_Apply_Orders(processor.orders_deleted);////
        processor.orders_deleted.clear();
        
        
        HashMap<String,Stat> laststats = TxToStats(processor.newtransactions);
        db.DB_Apply_Stats(laststats, processor.decimalScale);
        
        db.DB_Apply_Transactions(processor.newtransactions);
        processor.newtransactions.clear();
        
        
        db.DB_Close();
    }
    
    /* Returns last stats for last transactions */
    public HashMap<String,Stat> TxToStats(ArrayList<Transaction> txes)
    {
        long unixTime = System.currentTimeMillis() / 1000L;
        long periodTime = ((long)(unixTime/(60*60))) * (60*60);
        
        HashMap<String,Stat> stats = new HashMap<String,Stat>();
        for (Transaction tx : txes)
        {
            String txtradetype = "";
            BigDecimal tx_vol = BigDecimal.ZERO;
            if(!tx.fromcur.equals(tx.unitcur))
            {
                tx_vol = tx.amount;
                txtradetype = tx.unitcur+"2"+tx.fromcur;
            }
            else
            {
                tx_vol = tx.amount.divide(tx.unitprice, 10, BigDecimal.ROUND_DOWN);
                if(processor!=null)
                {
                    tx_vol = tx_vol.setScale(processor.decimalScale, BigDecimal.ROUND_DOWN);
                }
                txtradetype = tx.unitcur+"2"+tx.tocur;
            }
            
            if(!stats.containsKey(txtradetype))
            {
                Stat newstat = new Stat('h', txtradetype);
                newstat.stime = periodTime;
                newstat.status = '0';
                
                newstat.price_now = tx.unitprice;
                newstat.price_high = tx.unitprice;
                newstat.price_low = tx.unitprice;
                newstat.price_avg = BigDecimal.ZERO;
                
                stats.put(txtradetype, newstat);
            }
            Stat stat_i = stats.get(txtradetype);
            BigDecimal newavg = stat_i.price_avg.multiply(new BigDecimal(stat_i.trade_num));
            newavg = newavg.add(tx.unitprice);
            newavg = newavg.divide(new BigDecimal(stat_i.trade_num+1), 10, BigDecimal.ROUND_DOWN);
            newavg = newavg.setScale(processor.decimalScale, BigDecimal.ROUND_DOWN);
            
            stat_i.price_avg = newavg;
            stat_i.trade_num += 1;
            stat_i.trade_vol = stat_i.trade_vol.add(tx_vol);
            stat_i.price_now = tx.unitprice;
            
            if(tx.unitprice.compareTo(stat_i.price_high) > 0)
            {
                stat_i.price_high = tx.unitprice;
            }
            if(tx.unitprice.compareTo(stat_i.price_low) < 0)
            {
                stat_i.price_low = tx.unitprice;
            }            
        }
        
        return stats;
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
            //e.printStackTrace();
        }
        return rt;
    }
    
    public void ControlTest()
    {
        long newunixtime = UnixTime();
        if(newunixtime%2==0 && lastunixtime!=newunixtime)
        {
            lastunixtime = newunixtime;
            Print(" # > "+lastunixtime);
        }
    }
    
    public long UnixTime()
    {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime;
    }
    
    
    
    public void Test()
    {
        
        //DBConnector db = new DBConnector();
        //db.readDataBase();
        
        //debug performance
        long time1 = System.currentTimeMillis();
        //debug memory
        double memused1 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0d * 1024.0d);
        Print("Used Mem: "+memused1);
        
        
        
        Processor p = new Processor();
        p.TestUsers();
        p.TestOrders();
        p.ProcessOrders();
        
        //debug performance end
        long time2 = System.currentTimeMillis();
        long diff = time2 - time1;
        Print("Perf: "+diff/1000.0f);
        
        p.TestOrders();
        p.ProcessOrders();
        Print("-------");
        Print("USERS");
        p.TestPrintUsers();
        Print("-------");
        Print("BUY ORDERS");
        p.orderbooks.FindOrderBookByType("btc", "ltc").PrintFirst(10);
        Print("-------");
        Print("SELL ORDERS");
        p.orderbooks.FindOrderBookByType("ltc", "btc").PrintFirst(10);
        Print("-------");
        /*p.orderbooks.orderbooks.get(0).PrintFirst(10);
        p.orderbooks.orderbooks.get(1).PrintFirst(10);*/
        //p.TestUsers();
        //p.TestPrintUsers();
        
        /*
        BigDecimal bd = new BigDecimal("123456789123456789123456789123456789123456789123456789.12345678913456789");
        bd = bd.setScale(8, BigDecimal.ROUND_DOWN);
        Print("BD: "+bd);
        */
        
        /*
        Print("S1: "+p.orderbooks.orderbooks.get(0).orders.size());
        Print("I1: "+p.orderbooks.orderbooks.get(0).orders_i.size());
        for (int i = 100; i < 10000; i++)
        {
            p.orderbooks.DelOrder(i);
        }
        Print("S2: "+p.orderbooks.orderbooks.get(0).orders.size());
        Print("I2: "+p.orderbooks.orderbooks.get(0).orders_i.size());
        */
        /*p.users.DelUserByID(5);
        p.TestPrintUsers();*/
        
        //debug performance end
        long time3 = System.currentTimeMillis();
        long diff2 = time3 - time2;
        Print("Perf: "+diff2/1000.0f);
        
        
        //debug memory end
        double memused2 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0d * 1024.0d);
        Print("Used Mem: "+memused2);
        /*BigDecimal d1 = new BigDecimal("8485522.080310443");
        BigDecimal d2 = new BigDecimal("6939984.08586602");
        Print(d1.compareTo(d2));*/
    }
}
