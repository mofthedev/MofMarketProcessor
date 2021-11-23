package mofmarketprocessor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 *
 * @author mofselvi
 */
public class DBConnector {

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

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
