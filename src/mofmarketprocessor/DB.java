package mofmarketprocessor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mofselvi
 */
public class DB
{

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    
    private String db_host,db_user,db_pass,db_name;
    
    public DB(String host, String user, String pass, String name)
    {
        db_host = host;
        db_user = user;
        db_pass = pass;
        db_name = name;
    }
    
    public void DB_Connect()
    {
        try
        {
            Class.forName("org.mariadb.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mariadb://"+db_host+"/"+db_name+"?", db_user, db_pass);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void DB_ActivateManualCommit()
    {
        try
        {
            connect.setAutoCommit(false);
            statement = connect.createStatement();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void DB_ExecuteOnly(String query)
    {
        try
        {
            boolean returningRows = statement.execute(query);
            if(returningRows)
            {
                resultSet = statement.getResultSet();
                resultSet.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public ArrayList<HashMap<String,Object>> DB_Execute(String query)
    {
        ArrayList<HashMap<String,Object>> rows = new ArrayList<HashMap<String,Object>>();
        try
        {
            boolean returningRows = statement.execute(query);
            if(returningRows)
            {
                resultSet = statement.getResultSet();
            }
            else
            {
                return rows;
            }
            
            //get columns
            int columnCount = resultSet.getMetaData().getColumnCount();
            ArrayList<String> columns = new ArrayList<String>();
            ResultSetMetaData meta = resultSet.getMetaData();
            for (int i = 1; i <= columnCount; i++)
            {
                columns.add(meta.getColumnName(i));
            }
            
            //get rows
            while (resultSet.next())
            {
                HashMap<String,Object> row = new HashMap<String,Object>();
                for (String colName:columns)
                {
                    Object val = resultSet.getObject(colName);
                    row.put(colName,val);
                }
                rows.add(row);
            }
            
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return rows;
    }
    public void DB_Commit()
    {
        try
        {
            connect.commit();
            DB_ExecuteOnly("UNLOCK TABLES;");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void DB_Rollback()
    {
        try
        {
            connect.rollback();
            DB_ExecuteOnly("UNLOCK TABLES;");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void DB_Close()
    {
        try
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
            if (statement != null)
            {
                statement.close();
            }
            if (connect != null)
            {
                connect.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    

    //--------------------------------------------------------------------------
    
    public ArrayList<Order> DB_Fetch_Orders(char status)
    {
        ArrayList<Order> orderlist = new ArrayList<Order>();
        try
        {
            
            DB_ActivateManualCommit();
            DB_ExecuteOnly("LOCK TABLES bank_orders WRITE;");
        
            String wherestatus = " WHERE status='"+status+"' ";
            if(status=='*')
            {
                wherestatus = " ";
            }
            boolean returningRows = statement.execute("SELECT * FROM bank_orders "+wherestatus+" FOR UPDATE");
            if(returningRows)
            {
                resultSet = statement.getResultSet();
            }
            else
            {
                return orderlist;
            }
            
            //get rows
            while (resultSet.next())
            {
                long id_ = resultSet.getLong("id");
                long userid_ = resultSet.getLong("userid");
                BigDecimal unitprice_ = resultSet.getBigDecimal("unitprice");
                String unitcur_ = resultSet.getString("unitcur");
                BigDecimal amount_ = resultSet.getBigDecimal("amount");
                String fromcur_ = resultSet.getString("fromcur");
                String tocur_ = resultSet.getString("tocur");
                char status_ = resultSet.getString("status").charAt(0);
                long ctime_ = resultSet.getLong("ctime");
                BigDecimal stopprice_ = resultSet.getBigDecimal("stopprice");
                Order neworder = new Order(id_, userid_, unitprice_, unitcur_, amount_, fromcur_, tocur_, status_, ctime_, stopprice_);
                orderlist.add(neworder);
            }
            
            
            DB_Commit();
            
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
        catch(Exception e)
        {
            DB_Rollback();
            e.printStackTrace();
        }
        return orderlist;
    }
    
    public ArrayList<User> DB_Fetch_Users(char status)
    {
        ArrayList<User> userlist = new ArrayList<User>();
        try
        {
            DB_ActivateManualCommit();
            DB_ExecuteOnly("LOCK TABLES bank_users WRITE;");
            
            String wherestatus = " WHERE status='"+status+"' ";
            if(status=='*')
            {
                wherestatus = " ";
            }
            boolean returningRows = statement.execute("SELECT * FROM bank_users "+wherestatus+" FOR UPDATE");
            if(returningRows)
            {
                resultSet = statement.getResultSet();
            }
            else
            {
                return userlist;
            }
            
            
            //get columns
            ArrayList<String> amountnames = new ArrayList<String>();
            int columnCount = resultSet.getMetaData().getColumnCount();
            ResultSetMetaData meta = resultSet.getMetaData();
            for (int i = 1; i <= columnCount; i++)
            {
                String colname = meta.getColumnName(i);
                if(colname.toLowerCase().contains("amount_"))
                {
                    String newcurname = colname.replace("amount_", "");
                    amountnames.add(newcurname);
                }
            }
            
            //get rows
            while (resultSet.next())
            {
                long id_ = resultSet.getLong("id");
                long viptime_ = resultSet.getLong("viptime");
                long refedby_ = resultSet.getLong("refedby");
                char status_ = resultSet.getString("status").charAt(0);
                BigDecimal refbtc_ = resultSet.getBigDecimal("refbtc");
                
                Amounts amounts_ = new Amounts();
                for (String amountname : amountnames)
                {
                    BigDecimal amountval = resultSet.getBigDecimal("amount_"+amountname);
                    amounts_.AmountSet(amountname, amountval);
                }
                
                User newuser = new User(id_, viptime_, refedby_, status_, amounts_, refbtc_);
                userlist.add(newuser);
            }
            
            DB_Commit();
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
        catch(Exception e)
        {
            DB_Rollback();
            e.printStackTrace();
        }
        return userlist;
    }
    //**************************************************************************
    //Case, Users_changed, Orders_changed, Orders_deleted, new_Transactions
    public void DB_Apply_Case(Amounts casex)
    {
        DB_ActivateManualCommit();
        DB_ExecuteOnly("LOCK TABLES bank_case WRITE;");
        try
        {
            for (Map.Entry<String, BigDecimal> entry : casex.amounts.entrySet())
            {
                String curName = entry.getKey();
                BigDecimal curAmount = entry.getValue();
                
                resultSet = statement.executeQuery("SELECT count(*) as num FROM bank_case WHERE cointype='"+curName+"' FOR UPDATE");
                if(resultSet.next())
                {
                    int casenum = resultSet.getInt("num");
                    resultSet.close();
                    if(casenum > 0)
                    {
                        DB_ExecuteOnly("UPDATE bank_case SET amount=amount+"+curAmount+" WHERE cointype='"+curName+"' ");
                    }
                    else
                    {
                        DB_ExecuteOnly("INSERT INTO bank_case (cointype,amount,txamount,address) VALUES ('"+curName+"',"+curAmount+",0.0,'') ");
                    }
                }
            }
            DB_Commit();
            casex.ZeroAll();
        }
        catch(Exception e)
        {
            DB_Rollback();
            e.printStackTrace();
        }
    }
    
    public void DB_Apply_Users(ArrayList<User> usrs)
    {
        DB_ActivateManualCommit();
        DB_ExecuteOnly("LOCK TABLES bank_users WRITE;");
        try
        {
            for (User usr : usrs)
            {
                long id_ = usr.id;
                char status_ = usr.status;
                Amounts amounts_ = usr.amounts;
                BigDecimal refbtc_ = usr.refbtc;
                String setamounts = "";
                for (Map.Entry<String, BigDecimal> entry : amounts_.amounts.entrySet())
                {
                    //entry.getKey()
                    //entry.getValue()
                    setamounts += ", amount_"+entry.getKey()+"="+entry.getValue();
                }
                DB_ExecuteOnly("UPDATE bank_users SET refbtc="+refbtc_+", status='"+status_+"' "+setamounts+" WHERE id="+id_+" ");
            }
            DB_Commit();
        }
        catch(Exception e)
        {
            DB_Rollback();
            e.printStackTrace();
        }
    }
    
    public void DB_Apply_Orders(ArrayList<Order> ordrs)
    {
        DB_ActivateManualCommit();
        DB_ExecuteOnly("LOCK TABLES bank_orders WRITE;");
        try
        {
            for (Order ordr : ordrs)
            {
                if(ordr != null)
                {
                    long id_ = ordr.id;
                    BigDecimal amount_ = ordr.amount;
                    char status_ = ordr.status;
                    BigDecimal stopprice_ = ordr.stopprice;
                    DB_ExecuteOnly("UPDATE bank_orders SET amount="+amount_+",status='"+status_+"',stopprice="+stopprice_+" WHERE id="+id_+" ");
                }
            }
            DB_Commit();
        }
        catch(Exception e)
        {
            DB_Rollback();
            e.printStackTrace();
        }
    }
    
    public void DB_Apply_Transactions(ArrayList<Transaction> txes)
    {
        DB_ActivateManualCommit();
        DB_ExecuteOnly("LOCK TABLES bank_transactions WRITE;");
        try
        {
            for (Transaction tx : txes)
            {
                long makerid_ = tx.makerid;
                long takerid_ = tx.takerid;
                BigDecimal unitprice_ = tx.unitprice;
                String unitcur_ = tx.unitcur;
                BigDecimal amount_ = tx.amount;
                String fromcur_ = tx.fromcur;
                String tocur_ = tx.tocur;
                char status_ = tx.status;
                long ctime_ = tx.ctime;
                
                DB_ExecuteOnly("INSERT INTO bank_transactions (makerid,takerid,unitprice,unitcur,amount,fromcur,tocur,ctime) VALUES ("+makerid_+", "+takerid_+", "+unitprice_+", '"+unitcur_+"', "+amount_+", '"+fromcur_+"', '"+tocur_+"', "+ctime_+" )");
            }
            DB_Commit();
            txes.clear();
        }
        catch(Exception e)
        {
            DB_Rollback();
            e.printStackTrace();
        }
    }
    
    public void DB_Apply_Stats(HashMap<String,Stat> stats, int decimalScale)
    {
        DB_ActivateManualCommit();
        DB_ExecuteOnly("LOCK TABLES bank_stats WRITE;");
        try
        {
            for(Map.Entry<String, Stat> statentry : stats.entrySet())
            {
                String thestattype = statentry.getKey();
                Stat thestat = statentry.getValue();
                
                boolean statexists = false;
                boolean returningRows = statement.execute("SELECT * FROM bank_stats WHERE statperiod='"+thestat.statperiod+"' AND tradetype='"+thestat.tradetype+"' AND stime="+thestat.stime+" FOR UPDATE");
                if(returningRows)
                {
                    resultSet = statement.getResultSet();
                    while (resultSet.next())
                    {
                        statexists = true;
                        long id_ = resultSet.getLong("id");
                        BigDecimal price_open_ = resultSet.getBigDecimal("price_open");
                        BigDecimal price_high_ = resultSet.getBigDecimal("price_high");
                        BigDecimal price_low_ = resultSet.getBigDecimal("price_low");
                        BigDecimal price_close_ = resultSet.getBigDecimal("price_close");
                        BigDecimal price_avg_ = resultSet.getBigDecimal("price_avg");
                        long trade_num_ = resultSet.getLong("trade_num");
                        BigDecimal trade_vol_ = resultSet.getBigDecimal("trade_vol");
                        
                        if(price_high_.compareTo(thestat.price_high) > 0)
                        {
                            thestat.price_high = price_high_;
                        }
                        if(price_low_.compareTo(thestat.price_low) < 0)
                        {
                            thestat.price_low = price_low_;
                        }
                        
                        BigDecimal oldavg = price_avg_.multiply(new BigDecimal(trade_num_));
                        BigDecimal newavg = thestat.price_avg.multiply(new BigDecimal(thestat.trade_num));
                        BigDecimal avgtotal = newavg.add(oldavg);
                        long avgnumber = trade_num_+thestat.trade_num;
                        BigDecimal avgresult = avgtotal.divide(new BigDecimal(avgnumber), 10, BigDecimal.ROUND_DOWN);
                        avgresult = avgresult.setScale(decimalScale, BigDecimal.ROUND_DOWN);
                        thestat.price_avg = avgresult;
                        thestat.trade_num = avgnumber;
                    }
                }
                
                if(statexists)
                {
                    //UPDATE
                    //price_open="+thestat.price_now+", 
                    DB_ExecuteOnly("UPDATE bank_stats SET price_high="+thestat.price_high+", price_low="+thestat.price_low+", price_close="+thestat.price_now+", price_avg="+thestat.price_avg+", trade_num="+thestat.trade_num+", trade_vol=trade_vol+"+thestat.trade_vol+" WHERE statperiod='"+thestat.statperiod+"' AND tradetype='"+thestat.tradetype+"' AND stime="+thestat.stime+" ");
                }
                else
                {
                    //INSERT INTO
                    DB_ExecuteOnly("INSERT INTO bank_stats (statperiod, tradetype, stime, price_open, price_high, price_low, price_close, price_avg, trade_num, trade_vol) VALUES ('"+thestat.statperiod+"', '"+thestat.tradetype+"', "+thestat.stime+", "+thestat.price_now+", "+thestat.price_high+", "+thestat.price_low+", "+thestat.price_now+", "+thestat.price_avg+", "+thestat.trade_num+", "+thestat.trade_vol+")");
                }
                
                //DB_ExecuteOnly("INSERT INTO bank_stats () VALUES ( )");
            }
            DB_Commit();
            if (resultSet != null)
            {
                resultSet.close();
            }
            stats.clear();
        }
        catch(Exception e)
        {
            DB_Rollback();
            e.printStackTrace();
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    
    
    private ArrayList<HashMap<String,Object>> Query(String q)
    {
        ArrayList<HashMap<String,Object>> rows = new ArrayList<HashMap<String,Object>>();
        try
        {
            Class.forName("org.mariadb.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mariadb://"+db_host+"/"+db_name+"?", db_user, db_pass);
            statement = connect.createStatement();
            boolean returningRows = statement.execute(q);
            if(returningRows)
            {
                resultSet = statement.getResultSet();
            }
            else
            {
                return rows;
            }
            //get columns
            int columnCount = resultSet.getMetaData().getColumnCount();
            ArrayList<String> columns = new ArrayList<String>();
            ResultSetMetaData meta = resultSet.getMetaData();
            for (int i = 1; i <= columnCount; i++)
            {
                columns.add(meta.getColumnName(i));
            }
            //get rows
            while (resultSet.next())
            {
                HashMap<String,Object> row = new HashMap<String,Object>();
                for (String colName:columns)
                {
                    Object val = resultSet.getObject(colName);
                    row.put(colName,val);
                }
                rows.add(row);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            close();
        }
        return rows;
        /*
        for(HashMap<String,Object> row : rows)
        {
            for(Map.Entry<String, Object> entry : row.entrySet())
            {
                String key = entry.getKey();
                Object value = entry.getValue();
            }
        }
        */
    }

    public void readDataBase()
    {
        try
        {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("org.mariadb.jdbc.Driver");
            //org.mariadb.jdbc.Driver
            //com.mysql.jdbc.Driver
            // Setup the connection with the DB
            connect = DriverManager.getConnection("jdbc:mariadb://localhost/testdb?", "root", "3880mysql");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement.executeQuery("select * from testdb.table1 LIMIT 10");
            writeResultSet(resultSet);

            /*
            // PreparedStatements can use variables and are more efficient
            preparedStatement = connect.prepareStatement("insert into  feedback.comments values (default, ?, ?, ?, ? , ?, ?)");
            // "myuser, webpage, datum, summary, COMMENTS from feedback.comments");
            // Parameters start with 1
            preparedStatement.setString(1, "Test");
            preparedStatement.setString(2, "TestEmail");
            preparedStatement.setString(3, "TestWebpage");
            preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
            preparedStatement.setString(5, "TestSummary");
            preparedStatement.setString(6, "TestComment");
            preparedStatement.executeUpdate();
            */
            
            /*preparedStatement = connect.prepareStatement("SELECT myuser, webpage, datum, summary, COMMENTS from feedback.comments");
            resultSet = preparedStatement.executeQuery();
            writeResultSet(resultSet);*/

            // Remove again the insert comment
            /*preparedStatement = connect.prepareStatement("delete from feedback.comments where myuser= ? ; ");
            preparedStatement.setString(1, "Test");
            preparedStatement.executeUpdate();*/

            /*resultSet = statement.executeQuery("select * from feedback.comments");
            writeMetaData(resultSet);*/

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            close();
        }
    }

    private void writeMetaData(ResultSet resultSet) throws SQLException
    {
        System.out.println("The columns in the table are: ");
        System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++)
        {
            System.out.println("Column " + i + " " + resultSet.getMetaData().getColumnName(i));
        }
    }

    private void writeResultSet(ResultSet resultSet) throws SQLException
    {
        while (resultSet.next())
        {
            // It is possible to get the columns via name
            // also possible to get the columns via the column number
            // which starts at 1
            // e.g. resultSet.getSTring(2);
            String id = resultSet.getString("id");
            String fromcur = resultSet.getString("fromcur");
            String tocur = resultSet.getString("tocur");
            //Date date = resultSet.getDate("datum");
            BigDecimal amount = resultSet.getBigDecimal("amount");
            Integer ctime = resultSet.getInt("ctime");
            System.out.println("ID: " + id);
            System.out.println("Fromcur: " + fromcur);
            System.out.println("Tocur: " + tocur);
            System.out.println("Amount: " + amount.divide(new BigDecimal(2)));
            System.out.println("CTime: " + ctime);
        }
    }

    // You need to close the resultSet
    private void close()
    {
        try
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
            if (statement != null)
            {
                statement.close();
            }
            if (connect != null)
            {
                connect.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
