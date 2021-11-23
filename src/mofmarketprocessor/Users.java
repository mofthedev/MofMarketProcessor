package mofmarketprocessor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author mofselvi
 */
public class Users
{
    public ArrayList<User> users = new ArrayList<User>();
    
    private UserComparator userComparator;
    
    public Users()
    {
        userComparator = new UserComparator();
    }
    public void AddUser(long id, long viptime, long refedby, char status, Amounts amounts, BigDecimal refbtc_)
    {
        int userind = IndexOf(id);
        if(userind >= 0)
        {
            User olduser = Get(userind);
            olduser.viptime = viptime;
            olduser.amounts = amounts;
            olduser.refedby = refedby;
            return;
        }
        userind *= -1;
        userind -= 1;
        User newuser = new User(id, viptime, refedby, status, amounts, refbtc_);
        users.add(userind, newuser);
        //users.add(newuser);
        //Collections.sort(users, userComparator);
    }
    public int IndexOf(long id)
    {
        int ind = Collections.binarySearch(users, new User(id,0,0,'1', new Amounts(), BigDecimal.ZERO), userComparator);
        return ind;
    }
    public User Get(int index)
    {
        return users.get(index);
    }
    public User GetByID(long id)
    {
        int ind = IndexOf(id);
        if(ind >= 0)
        {
            return Get(ind);
        }
        return null;
    }
    
    public boolean UserBalanceAdd(long id, String currency, BigDecimal value)
    {
        User user = GetByID(id);
        if(user == null)
        {
            return false;
        }
        user.amounts.AmountAdd(currency, value);
        return true;
    }
    
    public boolean DelUserByID(long id)
    {
        int ind = IndexOf(id);
        if(ind >= 0)
        {
            DelUserByIndex(ind);
            return true;
        }
        return false;
    }
    public boolean DelUserByIndex(int index)
    {
        int size = users.size();
        if(index >= size || index < 0)
        {
            return false;
        }
        users.remove(index);
        return true;
    }
}

class UserComparator implements Comparator<User>
{
    @Override
    public int compare(User o1, User o2)
    {
        if(o1.id > o2.id)
        {
            return 1;
        }
        else if(o1.id < o2.id)
        {
            return -1;
        }
        return 0;
    }
}