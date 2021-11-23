package mofmarketprocessor;

import java.math.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mofselvi
 */
public class Amounts
{
    Map<String, BigDecimal> amounts;
    
    private int decimalScale = 8;
    
    public Amounts()
    {
        amounts = new HashMap<String, BigDecimal>();
    }
    
    public void AmountSet(String currency, BigDecimal value)
    {
        amounts.put(currency, value);
    }
    public BigDecimal AmountGet(String currency)
    {
        BigDecimal rt = amounts.get(currency);
        if(rt == null)
        {
            return BigDecimal.ZERO;
        }
        return rt;
    }
    
    public void AmountAdd(String currency, BigDecimal augend)
    {
        BigDecimal oldval = AmountGet(currency);
        BigDecimal newval = oldval.add(augend);
        newval = newval.setScale(decimalScale, BigDecimal.ROUND_DOWN);
        AmountSet(currency, newval);
    }
    public void AmountSub(String currency, BigDecimal subtrahend)
    {
        BigDecimal oldval = AmountGet(currency);
        BigDecimal newval = oldval.subtract(subtrahend);
        newval = newval.setScale(decimalScale, BigDecimal.ROUND_DOWN);
        AmountSet(currency, newval);
    }
    public void AmountMul(String currency, BigDecimal multiplicand)
    {
        BigDecimal oldval = AmountGet(currency);
        BigDecimal newval = oldval.multiply(multiplicand);
        newval = newval.setScale(decimalScale, BigDecimal.ROUND_DOWN);
        AmountSet(currency, newval);
    }
    public void AmountDiv(String currency, BigDecimal divisor)
    {
        BigDecimal oldval = AmountGet(currency);
        BigDecimal newval = oldval.divide(divisor, 10, BigDecimal.ROUND_DOWN);
        newval = newval.setScale(decimalScale, BigDecimal.ROUND_DOWN);
        AmountSet(currency, newval);
    }
    
    public ArrayList<String> Currencies()
    {
        ArrayList<String> curs = new ArrayList<String>();
        /*for (Map.Entry<String, BigDecimal> entry : amounts.entrySet())
        {
            curs.add(entry.getKey());
            //entry.getValue();
        }*/
        for ( String curr : amounts.keySet() )
        {
            curs.add(curr);
        }
        return curs;
    }
    
    public void ZeroAll()
    {
        for (Map.Entry<String, BigDecimal> entry : amounts.entrySet())
        {
            AmountSet(entry.getKey(), BigDecimal.ZERO);
        }
    }
    
    public String toString()
    {
        String rt = "(";
        for (Map.Entry<String, BigDecimal> entry : amounts.entrySet())
        {
            rt += entry.getKey()+":"+entry.getValue();
            rt += ",";
        }
        rt += ")";
        return rt;
    }
    
}
