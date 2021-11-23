/*
 * programmed by mofselvi
 */

package mofmarketprocessor;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 *
 * @author mofselvi
 */
public class Order
{
    public long id;
    public long userid;
    public BigDecimal unitprice;
    public String unitcur;
    public BigDecimal amount;
    public String fromcur;
    public String tocur;
    public char status;
    public long ctime;
    public BigDecimal stopprice;
    
    public String ordertype;
    
    private BigDecimal lsp = new BigDecimal("0.00000001");//least significant price
    
    public Order(long id_, long userid_, BigDecimal unitprice_, String unitcur_, BigDecimal amount_, String fromcur_, String tocur_, char status_, long ctime_, BigDecimal stopprice_)
    {
        this.id = id_;
        this.userid = userid_;
        this.unitprice = unitprice_;
        this.unitcur = unitcur_;
        this.amount = amount_;
        this.fromcur = fromcur_;
        this.tocur = tocur_;
        this.status = status_;
        this.ctime = ctime_;
        this.stopprice = stopprice_;
        
        this.ordertype = this.fromcur+"2"+this.tocur;
        
        this.unitprice = this.unitprice.setScale(8, BigDecimal.ROUND_DOWN);
        this.amount = this.amount.setScale(8, BigDecimal.ROUND_DOWN);
        
        this.lsp = new BigDecimal("0.00000001");
    }
    
    public Order(long id_)
    {
        this.id = id_;
        
        this.lsp = new BigDecimal("0.00000001");
    }
    
    public Order(BigDecimal unitprice_, BigDecimal stopprice_)
    {
        this.id = -1;
        this.unitprice = unitprice_;
        this.stopprice = stopprice_;
        
        this.lsp = new BigDecimal("0.00000001");
    }
    
    public String toString()
    {
        return "\n{"+this.id+","+this.userid+","+this.status+","+this.fromcur+"2"+this.tocur+",\t\tunit:"+this.unitprice+",amount:"+this.amount+",stop:"+this.stopprice+"}";
    }
    
    public boolean IsBuy()
    {
        if(unitcur.equalsIgnoreCase(fromcur))
        {
            return true;
        }
        return false;
    }
    
    public boolean IsSell()
    {
        return !IsBuy();
    }
    
    public boolean HasStop()
    {
        if(stopprice.compareTo(BigDecimal.ZERO) == 0)
        {
            return false;
        }
        return true;
    }
    
    public boolean IsStopLossOrder()
    {
        return HasStop();
    }
    
    public boolean IsMarketOrder()
    {
        if(unitprice.compareTo(BigDecimal.ZERO) == 0)
        {
            return true;
        }
        return false;
    }
    
    public boolean IsLimitOrder()
    {
        if(!HasStop() && !IsMarketOrder())
        {
            return true;
        }
        return false;
    }
    
    public boolean IsIneffective(BigDecimal minAmount)
    {
        //BigDecimal minAmount = new BigDecimal("0.0001");
        if(amount.compareTo(minAmount) == -1)
        {
            return true;
        }
        if(unitprice.compareTo(BigDecimal.ZERO) == 0)
        {
            return false;
        }
        if(unitprice.compareTo(lsp) == -1)
        {
            return true;
        }
        if(IsBuy())
        {
            if(amount.divide(unitprice, 10, BigDecimal.ROUND_DOWN).compareTo(minAmount) == -1)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            if(amount.multiply(unitprice).compareTo(minAmount) == -1)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
