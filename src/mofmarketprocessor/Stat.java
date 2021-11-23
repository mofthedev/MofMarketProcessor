/*
 * programmed by mofselvi
 */

package mofmarketprocessor;

import java.math.BigDecimal;

/**
 *
 * @author mofselvi
 */
public class Stat
{
    public char statperiod;
    public String tradetype;
    public long stime;
    public BigDecimal price_now, price_high, price_low;
    public BigDecimal price_avg;
    public long trade_num;
    public BigDecimal trade_vol;
    public char status;
    
    public Stat(char statperiod_, String tradetype_, long stime_, BigDecimal price_now_, BigDecimal price_high_, BigDecimal price_low_, BigDecimal price_avg_, long trade_num_, BigDecimal trade_vol_, char status_)
    {
        this.statperiod = statperiod_;
        this.tradetype = tradetype_;
        this.stime = stime_;
        this.price_now = price_now_;
        this.price_high = price_high_;
        this.price_low = price_low_;
        this.price_avg = price_avg_;
        this.trade_num = trade_num_;
        this.trade_vol = trade_vol_;
        this.status = status_;
    }
    public Stat(char statperiod_, String tradetype_)
    {
        this.statperiod = statperiod_;
        this.tradetype = tradetype_;
        this.stime = 0;
        this.price_now = BigDecimal.ZERO;
        this.price_high = BigDecimal.ZERO;
        this.price_low = BigDecimal.ZERO;
        this.price_avg = BigDecimal.ZERO;
        this.trade_num = 0;
        this.trade_vol = BigDecimal.ZERO;
        this.status = '0';
    }
}