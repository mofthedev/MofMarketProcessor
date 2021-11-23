/*
 * programmed by mofselvi
 */

package mofmarketprocessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.*;

/**
 *
 * @author mofselvi
 */
public class OrderBook
{
    public String unitcur, ordertype, fromcur, tocur;
    
    public ArrayList<Order> orders;
    public HashMap<Long, Order> orders_i;
    
    public ArrayList<Order> orders_s;
    public HashMap<Long, Order> orders_si;
    
    public BigDecimal currentprice;
    
    private BuyOrderComparator buyOrderComparator;
    private SellOrderComparator sellOrderComparator;
    
    private BuySOrderComparator buySOrderComparator;
    private SellSOrderComparator sellSOrderComparator;
    
    public OrderBook(String unitcur_, String fromcur_, String tocur_)
    {
        unitcur = unitcur_;
        ordertype = fromcur_+"2"+tocur_;
        fromcur = fromcur_;
        tocur = tocur_;
        
        orders = new ArrayList<Order>();
        orders_i = new HashMap<Long, Order>();
        
        orders_s = new ArrayList<Order>();
        orders_si = new HashMap<Long, Order>();
        
        currentprice = new BigDecimal("0.0");
        
        buyOrderComparator = new BuyOrderComparator();
        sellOrderComparator = new SellOrderComparator();
        
        buySOrderComparator = new BuySOrderComparator();
        sellSOrderComparator = new SellSOrderComparator();
    }
    
    public BigDecimal GetCurrentPrice()
    {
        return currentprice;
    }
    
    public void SetCurrentPrice(BigDecimal price)
    {
        currentprice = price;
    }
    
    
    //higher to lower
    public boolean IsBuy()
    {
        if(unitcur.equalsIgnoreCase(fromcur))
        {
            return true;
        }
        return false;
    }
    
    //lower to higher
    public boolean IsSell()
    {
        return !IsBuy();
    }
    
    public boolean CheckType(String fromcur, String tocur)
    {
        String conc = fromcur+"2"+tocur;
        if(conc.equalsIgnoreCase(ordertype))
        {
            return true;
        }
        return false;
    }
    
    public void Add(long id, long userid, BigDecimal unitprice, String unitcur, BigDecimal amount, String fromcur, String tocur, char status, long ctime, BigDecimal stopprice)
    {
        if(!CheckType(fromcur, tocur))
        {
            return;
        }
        Order oldorder = GetById(id);
        if(oldorder != null)
        {
            //oldorder.unitprice = unitprice;
            oldorder.amount = amount;
            oldorder.status = status;
            oldorder.stopprice = stopprice;
            return;
        }
        Order neworder = new Order(id, userid, unitprice, unitcur, amount, fromcur, tocur, status, ctime, stopprice);
        //if(status=='1')
        //{
        if(neworder.HasStop())
        {
            int ind = IndexOfStoploss(stopprice);
            ind *= -1;
            ind -= 1;
            orders_s.add(ind, neworder);
            orders_si.put(id, neworder);
        }
        else
        {
            int ind = IndexOfByUnitprice(unitprice);
            ind *= -1;
            ind -= 1;
            orders.add(ind, neworder);
            orders_i.put(id, neworder);
        }
        //}
    }
    
    public void Sort()
    {
        if(IsBuy())
        {
            Collections.sort(orders, buyOrderComparator);
        }
        else
        {
            Collections.sort(orders, sellOrderComparator);
        }
    }
    
    public void Sort_s()
    {
        if(IsBuy())
        {
            Collections.sort(orders_s, buySOrderComparator);
        }
        else
        {
            Collections.sort(orders_s, sellSOrderComparator);
        }
    }
    
    public boolean Delete(long id)
    {
        Order ind;
        ind = GetById(id);
        if(ind == null)
        {
            ind = GetById_S(id);
            if(ind==null)
            {
                return false;
            }
            orders_s.remove(ind);
            orders_si.remove(id);
            return true;
        }
        orders.remove(ind);
        orders_i.remove(id);
        //Mark as "d", set amount to ZERO
        return true;
    }
    
    public Order Get(int index)
    {
        return orders.get(index);
    }
    public Order GetById(long id)
    {
        return orders_i.get(id);
    }
    
    public Order Get_S(int index)
    {
        return orders_s.get(index);
    }
    public Order GetById_S(long id)
    {
        return orders_si.get(id);
    }
    
    public int IndexOfByUnitprice(BigDecimal unitprice)
    {
        int ind;
        if(IsBuy())
        {
            ind = Collections.binarySearch(orders, new Order(unitprice, BigDecimal.ZERO), buyOrderComparator);
        }
        else
        {
            ind = Collections.binarySearch(orders, new Order(unitprice, BigDecimal.ZERO), sellOrderComparator);
        }
        return ind;
    }
    public int IndexOfStoploss(BigDecimal stopprice)
    {
        int ind;
        if(IsBuy())
        {
            ind = Collections.binarySearch(orders_s, new Order(BigDecimal.ZERO, stopprice), buySOrderComparator);
        }
        else
        {
            ind = Collections.binarySearch(orders_s, new Order(BigDecimal.ZERO, stopprice), sellSOrderComparator);
        }
        return ind;
    }
    
    public void PrintFirst(int n)
    {
        int num = orders.size();
        if(n>=num)
        {
            n = num-1;
        }
        String symbol = ">";
        String listname = ":sell: ("+num+")";
        if(IsBuy())
        {
            symbol = "<";
            listname = ":buy: ("+num+")";
        }
        System.out.print(listname);
        for (int i = 0; i < n; i++)
        {
            System.out.print(""+orders.get(i)+" "+symbol);
        }
        System.out.println("");
    }
    
    public boolean NoOrder()
    {
        if(orders.size() < 1)
        {
            return true;
        }
        return false;
    }
    public boolean NoStop()
    {
        if(orders_s.size() < 1)
        {
            return true;
        }
        return false;
    }
    
    
    public Order OrderGetFirst()
    {
        if(orders == null)
        {
            return null;
        }
        if(orders.size() < 1)
        {
            return null;
        }
        Order rt = orders.get(0);
        return rt;
    }
}






class BuyOrderComparator implements Comparator<Order>
{
    @Override
    public int compare(Order o1, Order o2)
    {
        if(o1.unitprice == null)
        {
            if(o2.unitprice == null)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        else if(o2.unitprice == null)
        {
            return -1;
        }
        int rt = (o1.unitprice.compareTo(o2.unitprice)*-1);
        if(rt == 0 && o1.ctime > o2.ctime)
        {
            rt = -1;
        }
        return rt;
    }
}

class SellOrderComparator implements Comparator<Order>
{
    @Override
    public int compare(Order o1, Order o2)
    {
        if(o1.unitprice == null)
        {
            if(o2.unitprice == null)
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else if(o2.unitprice == null)
        {
            return 1;
        }
        int rt = o1.unitprice.compareTo(o2.unitprice);
        if(rt == 0 && o1.ctime > o2.ctime)
        {
            rt = -1;////?
        }
        return rt;
    }
}






//Stop-Loss Order Comparators
class BuySOrderComparator implements Comparator<Order>
{
    @Override
    public int compare(Order o1, Order o2)
    {
        if(o1.stopprice == null)
        {
            if(o2.stopprice == null)
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else if(o2.stopprice == null)
        {
            return 1;
        }
        int rt = o1.stopprice.compareTo(o2.stopprice);
        if(rt == 0 && o1.ctime > o2.ctime)
        {
            rt = -1;////?
        }
        return rt;
    }
}

class SellSOrderComparator implements Comparator<Order>
{
    @Override
    public int compare(Order o1, Order o2)
    {
        if(o1.stopprice == null)
        {
            if(o2.stopprice == null)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        else if(o2.stopprice == null)
        {
            return -1;
        }
        int rt = o1.stopprice.compareTo(o2.stopprice)*-1;
        if(rt == 0 && o1.ctime > o2.ctime)
        {
            rt = -1;
        }
        return rt;
    }
}