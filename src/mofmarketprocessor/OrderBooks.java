/*
 * programmed by mofselvi
 */

package mofmarketprocessor;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 *
 * @author mofselvi
 */
public class OrderBooks
{
    public ArrayList<OrderBook> orderbooks;
    
    public OrderBooks()
    {
        orderbooks = new ArrayList<OrderBook>();
    }
    
    public OrderBook FindOrderBookByType(String fromcur, String tocur)
    {
        for (OrderBook orderbook : orderbooks)
        {
            if(orderbook.CheckType(fromcur, tocur))
            {
                return orderbook;
            }
        }
        return null;
    }
    
    public void AddOrder(long id, long userid, BigDecimal unitprice, String unitcur, BigDecimal amount, String fromcur, String tocur, char status, long ctime, BigDecimal stopprice)
    {
        OrderBook properbook = FindOrderBookByType(fromcur, tocur);
        if(properbook == null)
        {
            OrderBook neworderbook = AddOrderBook(unitcur, fromcur, tocur);
            properbook = neworderbook;
        }
        
        properbook.Add(id, userid, unitprice, unitcur, amount, fromcur, tocur, status, ctime, stopprice);
    }
    
    public OrderBook AddOrderBook(String unitcur, String fromcur, String tocur)
    {
        OrderBook neworderbook = new OrderBook(unitcur, fromcur, tocur);
        orderbooks.add(neworderbook);
        return neworderbook;
    }
    
    public void Sort()
    {
        for (OrderBook thebook : orderbooks)
        {
            thebook.Sort();
        }
    }
    
    public void DelOrder(long id)
    {
        for (OrderBook orderbook : orderbooks)
        {
            Order ordertodel = orderbook.GetById(id);
            if(ordertodel != null)
            {
                orderbook.Delete(id);
                return;
            }
            else
            {
                ordertodel = orderbook.GetById_S(id);
                if(ordertodel != null)
                {
                    orderbook.Delete(id);
                    return;
                }
            }
        }
    }
    
    public Order GetOrder(long id)
    {
        for (OrderBook orderbook : orderbooks)
        {
            Order ordertoget = orderbook.GetById(id);
            if(ordertoget != null)
            {
                return ordertoget;
            }
        }
        return null;
    }
    public Order GetOrder_S(long id)
    {
        for (OrderBook orderbook : orderbooks)
        {
            Order ordertoget = orderbook.GetById_S(id);
            if(ordertoget != null)
            {
                return ordertoget;
            }
        }
        return null;
    }
    
    public void SetCurrentPrice(String fromcur, String tocur, BigDecimal currentprice)
    {
        OrderBook book1 = FindOrderBookByType(fromcur, tocur);
        OrderBook book2 = FindOrderBookByType(tocur, fromcur);
        if(book1!=null)
        {
            book1.SetCurrentPrice(currentprice);
        }
        if(book2!=null)
        {
            book2.SetCurrentPrice(currentprice);
        }
    }
    public BigDecimal GetCurrentPrice(String fromcur, String tocur)
    {
        OrderBook book1 = FindOrderBookByType(fromcur, tocur);
        OrderBook book2 = FindOrderBookByType(tocur, fromcur);
        if(book1!=null)
        {
            return book1.GetCurrentPrice();
        }
        if(book2!=null)
        {
            return book2.GetCurrentPrice();
        }
        return BigDecimal.ZERO;
    }
    
}

