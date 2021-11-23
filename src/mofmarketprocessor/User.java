package mofmarketprocessor;

import java.math.*;
import java.util.*;

/**
 *
 * @author mofselvi
 */
public class User
{
    public long id;
    public long viptime;
    public long refedby;
    public Amounts amounts;
    public char status;
    public BigDecimal refbtc;
    
    public User(long id_, long viptime_, long refedby_, char status_, Amounts amounts_, BigDecimal refbtc_)
    {
        amounts = new Amounts();
        this.id = id_;
        this.viptime = viptime_;
        this.refedby = refedby_;
        this.status = status_;
        this.amounts = amounts_;
        this.refbtc = refbtc_;
    }
    
    public String toString()
    {
        return "{"+this.id+","+this.status+","+this.amounts+"}";
    }
    
}
