package quentinc.sql;
import java.sql.*;
public class MySQLConnectionFactory implements ConnectionFactory {
private final String host, dbname,  user, pass;
public MySQLConnectionFactory (String h, String d, String u, String p) {
host=h; 
dbname=d;
user=u;
pass=p;
}
public Connection newConnection () throws ClassNotFoundException, SQLException {
Class.forName("com.mysql.jdbc.Driver");
return DriverManager.getConnection( String.format("jdbc:mysql://%s/%s?user=%s&password=%s", host, dbname, user, pass));
}
}
