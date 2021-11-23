/*
 * programmed by mofselvi
 */

package mofmarketprocessor;

import java.math.BigDecimal;

/**
 *
 * @author mofselvi
 */
public class Transaction
{
    public long makerid, takerid;
    public BigDecimal unitprice;
    public String unitcur;
    public BigDecimal amount;
    public String fromcur, tocur;
    public char status;
    public long ctime;
    
    public Transaction(long makerid_, long takerid_, BigDecimal unitprice_, String unitcur_, BigDecimal amount_, String fromcur_, String tocur_, char status_, long ctime_)
    {
        this.makerid = makerid_;
        this.takerid = takerid_;
        this.unitprice = unitprice_;
        this.unitcur = unitcur_;
        this.amount = amount_;
        this.fromcur = fromcur_;
        this.tocur = tocur_;
        this.status = status_;
        this.ctime = ctime_;
    }
}
