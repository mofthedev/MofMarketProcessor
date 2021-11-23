package mofmarketprocessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author mofselvi
 */
public class Processor
{
    public Amounts casex;
    public Users users;
    public OrderBooks orderbooks;
    public ArrayList<Order> orders_p;//orders waiting to be performed
    public ArrayList<Order> orders_d;//orders waiting to be deleted
    public ArrayList<Order> orders_s2lp;//old stoploss new limit orders
    
    public ArrayList<User> users_p;//users waiting to be changed
    public ArrayList<User> users_changed;
    public ArrayList<Order> orders_changed;
    public ArrayList<Order> orders_deleted;
    public ArrayList<Transaction> newtransactions;
    
    private BigDecimal minAmount;
    public final int decimalScale = 8;
    
    private BigDecimal commissionRateMaker, commissionRateTaker;
    private BigDecimal refcommrate;
    
    public Processor()
    {
        casex = new Amounts();//->empty
        users = new Users();
        orderbooks = new OrderBooks();
        orders_p = new ArrayList<Order>();//->fill
        orders_d = new ArrayList<Order>();//->fill
        orders_s2lp = new ArrayList<Order>();
        
        users_p = new ArrayList<User>();//->fill
        users_changed = new ArrayList<User>();//->empty
        orders_changed = new ArrayList<Order>();//->empty
        orders_deleted = new ArrayList<Order>();//->empty
        newtransactions = new ArrayList<Transaction>();//->empty
        
        minAmount = new BigDecimal("0.0001");
        commissionRateMaker = new BigDecimal("0.001");//=1/1000
        commissionRateTaker = new BigDecimal("0.001");//=1/1000
        refcommrate = new BigDecimal("0.2");
    }
    
    public void LoadUser(User theuser)
    {
        users.AddUser(theuser.id, theuser.viptime, theuser.refedby, theuser.status, theuser.amounts, theuser.refbtc);
    }
    
    public void LoadUsers(ArrayList<User> allusers)
    {
        for (User theuser : allusers)
        {
            users.AddUser(theuser.id, theuser.viptime, theuser.refedby, theuser.status, theuser.amounts, theuser.refbtc);
        }
    }
    
    public void LoadOrder(Order neworder)
    {
        orderbooks.AddOrder(neworder.id, neworder.userid, neworder.unitprice, neworder.unitcur, neworder.amount, neworder.fromcur, neworder.tocur, neworder.status, neworder.ctime, neworder.stopprice);
        //orderbooks.Sort();
    }
    
    public void LoadOrders(ArrayList<Order> neworders)
    {
        for (Order neworder : neworders)
        {
            orderbooks.AddOrder(neworder.id, neworder.userid, neworder.unitprice, neworder.unitcur, neworder.amount, neworder.fromcur, neworder.tocur, neworder.status, neworder.ctime, neworder.stopprice);
        }
        //orderbooks.Sort();
    }
    
    public void DelOrder(Order oldorder)
    {
        orderbooks.DelOrder(oldorder.id);
    }
    
    public void DelOrders(ArrayList<Order> oldorders)
    {
        for (Order oldorder : oldorders)
        {
            orderbooks.DelOrder(oldorder.id);
        }
    }
    
    
    
    public void TestOrders()
    {
        String[][] testtype = {{"btc","ltc"}, {"ltc","btc"}};
        ArrayList<Order> testorders = new ArrayList<Order>();
        for (int i = 0; i < 50000; i++)
        {
            double randomunitprice = (Math.random() *100);
            randomunitprice = randomunitprice*1000 + randomunitprice/100;
            double randomamount = (Math.random() *100)/100;
            
            int rnduserid = new Random().nextInt(10);
            int rndtype = new Random().nextInt(testtype.length);
            String[] typ = testtype[rndtype];
            //System.out.println("> "+new BigDecimal(randomunitprice)+" : "+randomunitprice);
            testorders.add(
                    new Order(
                            i, rnduserid, new BigDecimal(randomunitprice), "btc", new BigDecimal(randomamount), typ[0], typ[1], '1', i, BigDecimal.ZERO
                    )
            );
        }
        orders_p = testorders;
        //LoadOrders(testorders);
        System.out.println("OrderList");
        //System.out.println(testorders);
        //System.out.println(orderbooks.FindOrderBookByType("btc", "ltc").orders);
    }
    public void TestUsers()
    {
        for(int i=10; i>0; i--)
        {
            /*if(i==7)
                continue;*/
            
            //int random = (int )(Math.random() * 100000 + 1);
            //users.AddUser(random, random, 0, '1', new Amounts());
            Amounts newuseramt = new Amounts();
            newuseramt.AmountSet("btc", new BigDecimal("10000000.0"));
            newuseramt.AmountSet("ltc", new BigDecimal("10000000.0"));
            users.AddUser(i, 0, 0, '1', newuseramt, BigDecimal.ZERO);
        }
        //users.AddUser(7, 7, 0, '1', new Amounts());
    }
    public void TestPrintUsers()
    {
        for (User user : users.users)
        {
            System.out.println(user);
        }
    }
    
    
    
    public void ProcessOrders()
    {
        if(users_p!=null && users_p.size() > 0)
        {
            for (User userp : users_p)
            {
                qUserChange(userp);
            }
            LoadUsers(users_p);
            users_p.clear();
        }
        if(orders_d!=null)
        {
            for (Order orderd : orders_d)
            {
                Order ordertodel = orderbooks.GetOrder(orderd.id);
                if(ordertodel==null)
                {
                    ordertodel = orderbooks.GetOrder_S(orderd.id);
                    if(ordertodel==null)
                    {
                        System.out.println("Order "+orderd.id+" not found!");
                        orders_deleted.add(orderd);
                        continue;
                    }
                }
                User useroforderd = users.GetByID(ordertodel.userid);
                if(useroforderd==null)
                {
                    continue;
                }
                useroforderd.amounts.AmountAdd(ordertodel.fromcur, ordertodel.amount);
                ordertodel.status = 'd';
                users_changed.add(useroforderd);
                orders_deleted.add(ordertodel);
                orderbooks.DelOrder(ordertodel.id);
            }
            orders_d.clear();
        }
        if(orders_p==null)
        {
            return;
        }
        
        while(true)//for (Order orderp : orders_p)
        {
            while(orders_s2lp.size() != 0)
            {
                int getlasts2lp = orders_s2lp.size()-1;
                Order oldslo = orders_s2lp.get(getlasts2lp);
                orders_p.add(0, oldslo);
                orders_s2lp.remove(getlasts2lp);
            }
            if(orders_p.size() == 0)
            {
                break;
            }
            Order orderp = orders_p.get(0);
            //System.out.println("New Order Performing id:"+orderp.id);
            Order ordertoperform = orderbooks.GetOrder(orderp.id);
            if(ordertoperform!=null)
            {
                System.out.println("ERR already existing order");
                qOrderDelete(orderp);
                orders_p.remove(0);
                continue;
            }
            
            User useroforderp = users.GetByID(orderp.userid);
            if(useroforderp==null)
            {
                System.out.println("ERR no order owner");
                qOrderDelete(orderp);
                orders_p.remove(0);
                continue;
            }
            
            String findordertype = orderp.fromcur+"2"+orderp.tocur;
            OrderBook bookthis = orderbooks.FindOrderBookByType(orderp.fromcur, orderp.tocur);
            OrderBook bookcross = orderbooks.FindOrderBookByType(orderp.tocur, orderp.fromcur);
            if(bookthis == null)
            {
                bookthis = orderbooks.AddOrderBook(orderp.unitcur, orderp.fromcur, orderp.tocur);
            }
            if(bookcross == null)
            {
                bookcross = orderbooks.AddOrderBook(orderp.unitcur, orderp.tocur, orderp.fromcur);
            }
            
            
            //if this is a stoploss order, save it and continue
            int orderisstoploss = orderp.stopprice.compareTo(BigDecimal.ZERO);
            if(orderisstoploss == 1)
            {
                orderp.status = '1';
                orders_changed.add(orderp);
                bookthis.Add(orderp.id, orderp.userid, orderp.unitprice, orderp.unitcur, orderp.amount, orderp.fromcur, orderp.tocur, orderp.status, orderp.ctime, orderp.stopprice);
                orders_p.remove(0);
                
                useroforderp.amounts.AmountSub(orderp.fromcur, orderp.amount);
                qUserChange(useroforderp);
                
                continue;
            }
            
            //check if this is a market order or a limit order
            int ordert = orderp.unitprice.compareTo(BigDecimal.ZERO);
            /*if(ordert == 0)
            {
                Pop(orderp, useroforderp, bookthis, bookcross);
            }
            else if(ordert > 0)
            {
                Process(orderp, useroforderp, bookthis, bookcross);
            }*/
            if(ordert >= 0)
            {
                Process(orderp, useroforderp, bookthis, bookcross);
                continue;
            }
            else
            {
                System.out.println("ERR unitprice less than ZERO");
                qOrderDelete(orderp);
                orders_p.remove(0);
                continue;
            }
        }
        orders_p.clear();
    }
    
    public void Process(Order orderp, User useroforderp, OrderBook bookthis, OrderBook bookcross)
    {
        if(orderp.IsIneffective(minAmount))
        {
            System.out.println("WRN ineffective order amount orderid:"+orderp.id);
            qOrderDelete(orderp);
            orders_p.remove(0);
            return;
        }
        
        //check if the users balance is enough
        BigDecimal useramt = useroforderp.amounts.AmountGet(orderp.fromcur);
        if(useramt.compareTo(orderp.amount) == -1)
        {
            System.out.println("ERR order owning user balance not enough");
            qOrderDelete(orderp);
            orders_p.remove(0);
            return;
        }
        
        //System.out.println("/*/*/*/*/*/*/*/*/*/ TEST DEBUG");
        //discrement the balance of the user
        useroforderp.amounts.AmountSub(orderp.fromcur, orderp.amount);
        qUserChange(useroforderp);
        
        //get an order from the cross order book
        Order xorder = bookcross.OrderGetFirst();
        //if there is no cross order
        if(xorder==null)
        {
            //if this is not a market order, then save it
            if(!orderp.IsMarketOrder())
            {
                //System.out.println("WRN no cross liquidity, adding limit/stop order");
                //orderp.status = '1';
                //orders_changed.add(orderp);
                qOrderChange(orderp);
                bookthis.Add(orderp.id, orderp.userid, orderp.unitprice, orderp.unitcur, orderp.amount, orderp.fromcur, orderp.tocur, '1', orderp.ctime, orderp.stopprice);
            }
            else
            {
                //System.out.println("WRN no cross liquidity, deleting market order");
                useroforderp.amounts.AmountAdd(orderp.fromcur, orderp.amount);
                qUserChange(useroforderp);
                qOrderDelete(orderp);
            }
            ///save order
            //orders_changed.add(orderp);
            //delete from p list
            orders_p.remove(0);
            return;
        }
        User userofxorder = users.GetByID(xorder.userid);
        
        
        BigDecimal estimated = BigDecimal.ZERO;
        BigDecimal diff = BigDecimal.ZERO;
        if(orderp.IsBuy())
        {
            //xorder.unitprice > orderp.unitprice
            if(!orderp.IsMarketOrder() && xorder.unitprice.compareTo(orderp.unitprice) == 1)
            {
                //System.out.println("WRN filling liquidity for Buy");
                orderp.status = '0';////1
                orders_changed.add(orderp);
                bookthis.Add(orderp.id, orderp.userid, orderp.unitprice, orderp.unitcur, orderp.amount, orderp.fromcur, orderp.tocur, orderp.status, orderp.ctime, orderp.stopprice);
                orders_p.remove(0);
                return;
            }
            estimated = xorder.amount.multiply(xorder.unitprice);
            diff = orderp.amount.divide(xorder.unitprice, 10, BigDecimal.ROUND_DOWN);
        }
        else
        {
            //xorder.unitprice > orderp.unitprice
            if(!orderp.IsMarketOrder() && xorder.unitprice.compareTo(orderp.unitprice) == -1)
            {
                //System.out.println("WRN filling liquidity for Sell");
                orderp.status = '0';////1
                orders_changed.add(orderp);
                bookthis.Add(orderp.id, orderp.userid, orderp.unitprice, orderp.unitcur, orderp.amount, orderp.fromcur, orderp.tocur, orderp.status, orderp.ctime, orderp.stopprice);
                orders_p.remove(0);
                return;
            }
            estimated = xorder.amount.divide(xorder.unitprice, 10, BigDecimal.ROUND_DOWN);
            diff = orderp.amount.multiply(xorder.unitprice);
        }
        estimated = estimated.setScale(decimalScale, BigDecimal.ROUND_DOWN);
        diff = diff.setScale(decimalScale, BigDecimal.ROUND_DOWN);
        
        if(orderp.amount.compareTo(estimated) == 1)
        {
            BigDecimal amountexchanged = xorder.amount;
            BigDecimal amountUserTaker = CommissionHold(xorder.fromcur, xorder.amount, useroforderp, commissionRateTaker, xorder.unitprice, xorder.ordertype);
            BigDecimal amountUserMaker = CommissionHold(xorder.tocur, estimated, userofxorder, commissionRateMaker, xorder.unitprice, xorder.ordertype);
            
            useroforderp.amounts.AmountAdd(xorder.fromcur, amountUserTaker);
            userofxorder.amounts.AmountAdd(xorder.tocur, amountUserMaker);
            
            orderp.amount = orderp.amount.subtract(estimated);
            orderp.amount = orderp.amount.setScale(decimalScale, BigDecimal.ROUND_DOWN);
            orderp.status = '0';//orderp changed
            
            //mark old order as to be deleted and remove it
            qOrderDelete(xorder);
            orderbooks.DelOrder(xorder.id);
            
            //insert transaction
            ///4:xorder.amount -> amountexchanged
            Transaction newtx = new Transaction(userofxorder.id, useroforderp.id, xorder.unitprice, orderp.unitcur, amountexchanged, orderp.tocur, orderp.fromcur, '0', UnixTime());
            newtransactions.add(newtx);
            
            //set last prices
            bookthis.currentprice = xorder.unitprice;
            bookcross.currentprice = xorder.unitprice;
            StopToLimit(bookthis);
            StopToLimit(bookcross);
            
            //no need to @recursive this
            //Process(orderp, useroforderp, bookthis, bookcross);
            
            if(orderp.IsIneffective(minAmount))
            {
                useroforderp.amounts.AmountAdd(orderp.fromcur, orderp.amount);
                //qUserChange(useroforderp);///I added this later idk why//ok no need this
                qOrderDelete(orderp);
                orders_p.remove(0);
            }
            else
            {
                useroforderp.amounts.AmountAdd(orderp.fromcur, orderp.amount);
                //qUserChange(useroforderp);
            }
            
            qUserChange(useroforderp);
            qUserChange(userofxorder);
        }
        else
        {
            BigDecimal amountUserTaker = CommissionHold(xorder.fromcur, diff, useroforderp, commissionRateTaker, xorder.unitprice, xorder.ordertype);
            BigDecimal amountUserMaker = CommissionHold(xorder.tocur, orderp.amount, userofxorder, commissionRateMaker, xorder.unitprice, xorder.ordertype);
            
            useroforderp.amounts.AmountAdd(xorder.fromcur, amountUserTaker);
            userofxorder.amounts.AmountAdd(xorder.tocur, amountUserMaker);
            
            //insert transaction
            Transaction newtx = new Transaction(userofxorder.id, useroforderp.id, xorder.unitprice, orderp.unitcur, diff, orderp.tocur, orderp.fromcur, '0', UnixTime());
            newtransactions.add(newtx);
            
            //set last prices
            bookthis.currentprice = xorder.unitprice;
            bookcross.currentprice = xorder.unitprice;
            StopToLimit(bookthis);
            StopToLimit(bookcross);
            
            xorder.amount = xorder.amount.subtract(diff);
            
            if(xorder.IsIneffective(minAmount))
            {
                userofxorder.amounts.AmountAdd(xorder.fromcur, xorder.amount);
                qOrderDelete(xorder);
                bookcross.Delete(xorder.id);
            }
            else
            {
                qOrderChange(xorder);
            }
            
            
            qOrderDelete(orderp);
            orders_p.remove(0);
            
            
            qUserChange(useroforderp);
            qUserChange(userofxorder);
        }
        
    }
    
    /*public void Pop(Order orderp, User useroforderp, OrderBook bookthis, OrderBook bookcross)
    {
        
    }*/
    
    public void StopToLimit(OrderBook book)
    {
        while(true)
        {
            if(book.orders_s.size() < 1)
            {
                break;
            }
            Order sordr = book.Get_S(0);
            if( book.currentprice.compareTo(BigDecimal.ZERO) == 1 && ((sordr.IsBuy() && book.currentprice.compareTo(sordr.stopprice) != -1) || (sordr.IsSell() && book.currentprice.compareTo(sordr.stopprice) != 1)) )
            {
                User userofs2lo = users.GetByID(sordr.userid);
                if(userofs2lo==null)
                {
                    System.out.println("ERR no s2l order owner orderid:"+sordr.id);
                    book.Delete(sordr.id);
                    //qOrderDelete(sordr);
                }
                else
                {
                    sordr.stopprice = BigDecimal.ZERO;
                    qOrderChange(sordr);
                    book.Delete(sordr.id);
                    orders_s2lp.add(0, sordr);
                    
                    userofs2lo.amounts.AmountAdd(sordr.fromcur, sordr.amount);
                    qUserChange(userofs2lo);
                }
                
            }
            else
            {
                break;
            }
        }
    }
    
    public BigDecimal CommissionHold(String cur, BigDecimal amount, User usr, BigDecimal rate, BigDecimal unitpr, String ordrtyp)
    {
        if(usr.viptime >= UnixTime())
        {
            rate = BigDecimal.ZERO;
        }
        BigDecimal amountToHold = amount.multiply(rate);
        amountToHold = amountToHold.setScale(decimalScale, BigDecimal.ROUND_DOWN);
        BigDecimal amountTotalHolding = amountToHold;
        //System.out.println("ComHld userid:"+usr.id+" refid:"+usr.refedby);
        BigDecimal refamt = BigDecimal.ZERO;
        if(usr.refedby > 0 && !cur.equals("brgn"))
        {
            User refuser = users.GetByID(usr.refedby);
            refamt = amountToHold.multiply(refcommrate);
            refamt = refamt.setScale(decimalScale, BigDecimal.ROUND_DOWN);
            refuser.amounts.AmountAdd(cur, refamt);
            
            //user referral profit
            if(cur.equals("btc"))
            {
                refuser.refbtc = refuser.refbtc.add(refamt);
                refuser.refbtc = refuser.refbtc.setScale(decimalScale, BigDecimal.ROUND_DOWN);
            }
            else
            {
                String[] otypes = ordrtyp.split("2");
                if(otypes.length >= 2)
                {
                    if(otypes[0].equals("btc"))
                    {
                        BigDecimal refamt2 = refamt.multiply(unitpr);
                        refamt2 = refamt2.setScale(decimalScale, BigDecimal.ROUND_DOWN);
                        refuser.refbtc = refuser.refbtc.add(refamt2);
                        refuser.refbtc = refuser.refbtc.setScale(decimalScale, BigDecimal.ROUND_DOWN);
                    }
                    else if(otypes[1].equals("btc"))
                    {
                        BigDecimal refamt2 = refamt.divide(unitpr, 10, BigDecimal.ROUND_DOWN);
                        refamt2 = refamt2.setScale(decimalScale, BigDecimal.ROUND_DOWN);
                        refuser.refbtc = refuser.refbtc.add(refamt2);
                        refuser.refbtc = refuser.refbtc.setScale(decimalScale, BigDecimal.ROUND_DOWN);
                    }
                }
            }
            
            qUserChange(refuser);
            amountToHold = amountToHold.subtract(refamt);
        }
        
        casex.AmountAdd(cur, amountToHold);
        
        BigDecimal rt = amount.subtract(amountTotalHolding);
        return rt;
    }
    
    public void qOrderDelete(Order ordr)
    {
        ordr.amount = BigDecimal.ZERO;
        ordr.status = 'd';
        if(orders_deleted.indexOf(ordr) < 0)
        {
            orders_deleted.add(ordr);
        }
    }
    public void qOrderChange(Order ordr)
    {
        ordr.status = '0';
        if(orders_changed.indexOf(ordr) < 0)
        {
            orders_changed.add(ordr);
        }
    }
    public void qUserChange(User usr)
    {
        usr.status = '0';
        if(users_changed.indexOf(usr) < 0)
        {
            users_changed.add(usr);
        }
    }
    
    public long UnixTime()
    {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime;
    }
    
}
