package quentinc.sql;
import java.sql.*;

public interface ConnectionFactory {
public Connection newConnection () throws ClassNotFoundException, SQLException ;
}