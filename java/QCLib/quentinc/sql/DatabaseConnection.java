package quentinc.sql;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.io.*;

public class DatabaseConnection {
private static DatabaseConnection defaultConnection;
private final Map<String,PreparedStatement> rqs = new HashMap<String,PreparedStatement>();
private final Map<Class<? extends Record>, String> deleteStatements = new HashMap<Class<? extends Record>,String>();
private final Map<Class<? extends Record>, String> saveStatements = new HashMap<Class<? extends Record>,String>();
private ConnectionFactory cf;
private Connection con, lcon;
private boolean autocommit = true;

public static DatabaseConnection getDefaultConnection () { return defaultConnection; }
public static void setDefaultConnection (DatabaseConnection dbc) { defaultConnection=dbc; }
public static DatabaseConnection newMySQLConnection (String host, String dbname, String user, String pass) {
return new DatabaseConnection(new MySQLConnectionFactory(host, dbname, user, pass));
}

public DatabaseConnection () { this(null); }
public DatabaseConnection (ConnectionFactory cf1) { 
defaultConnection = this;
cf=cf1; 
}
public ConnectionFactory getConnectionFactory () { return cf; }
public void setConnectionFactory (ConnectionFactory cf1) { cf=cf1; }
public synchronized Connection getConnection () {
if (con==null) try {
if (cf==null) throw new IllegalStateException("No ConnectionFactory set");
con = cf.newConnection();
if (!autocommit) con.setAutoCommit(false);
lcon=null;
} catch (Exception e) {}
return con;
}

public synchronized void close () {
for (PreparedStatement p : rqs.values()) try { p.close(); } catch (Exception e) { }
try { if (con!=null) con.close(); } catch (Exception e) { }
rqs.clear();
lcon=con = null;
}

private PreparedStatement getPreparedStatement (String sql) throws SQLException {
PreparedStatement p = rqs.get(sql);
if (p==null || p.isClosed()) {
if (p!=null) p.close();
p = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
rqs.put(sql,p);
}
return p;
}
private PreparedStatement fillStatement (String sql, Object... obs) throws SQLException {
PreparedStatement p = getPreparedStatement(sql);
for (int i=0; i<obs.length; i++) p.setObject(i+1, obs[i]);
return p;
}

public synchronized ResultSet executeQuery (String sql, Object... args) { return executeQuery(0, sql, args); }
public synchronized int executeUpdate  (String sql, Object... args) { return executeUpdate(0, sql, args); }
public synchronized ResultSet executeInsert  (String sql, Object... args) { return executeInsert(0, sql, args); }

public synchronized ResultSet executeSingleQuery (String sql, Object... args) {
try {
return getConnection().createStatement().executeQuery(String.format(sql, args));
} catch (SQLException e) {  quentinc.util.Misc.throwUncheckedException(e); }
return null;
}
public synchronized int executeSingleUpdate (String sql, Object... args) {
Statement stmt = null;
try {
stmt = getConnection().createStatement();
int re = stmt.executeUpdate(String.format(sql, args));
return re;
} catch (SQLException e) {  quentinc.util.Misc.throwUncheckedException(e); }
finally { try {if (stmt!=null) stmt.close(); } catch (SQLException e) {}}
return -1;
}
public synchronized ResultSet executeSingleInsert  (String sql, Object... args) {
Statement stmt = null;
try {
stmt = getConnection().createStatement();
stmt.executeUpdate(String.format(sql, args), Statement.RETURN_GENERATED_KEYS);
return stmt.getGeneratedKeys();
} catch (SQLException e) {  quentinc.util.Misc.throwUncheckedException(e); }
return null;
}

private ResultSet executeQuery (int recursion, String sql, Object... obs) {
try {
return fillStatement(sql, obs).executeQuery();
} catch (SQLException e) {
close();
if (recursion<=3) return executeQuery(recursion+1, sql, obs);
else quentinc.util.Misc.throwUncheckedException(e);
return null;
}}
private int executeUpdate (int recursion, String sql, Object... obs) {
try {
return fillStatement(sql, obs).executeUpdate();
} catch (SQLException e) {
close();
if (recursion<=3) return executeUpdate(recursion+1, sql, obs);
else quentinc.util.Misc.throwUncheckedException(e);
return -1;
}}
private ResultSet executeInsert (int recursion, String sql, Object... obs) {
try {
PreparedStatement p = fillStatement(sql, obs);
p.executeUpdate();
return p.getGeneratedKeys();
} catch (SQLException e) {
close();
if (recursion<=3) return executeInsert(recursion+1, sql, obs);
else quentinc.util.Misc.throwUncheckedException(e);
return null;
}}

public boolean getAutoCommit () { return autocommit; }
public void setAutoCommit (boolean b) {
try {
if (con!=null) con.setAutoCommit(b);
autocommit=b;
lcon=autocommit?null:con;
} catch (SQLException e) { quentinc.util.Misc.throwUncheckedException(e); }
}
public void rollback () {
try {
if (autocommit) return;
if (con!=null) con.rollback();
lcon=con;
} catch (SQLException e) { quentinc.util.Misc.throwUncheckedException(e); }
}
public void commit () {
try {
if (autocommit) return;
if (con!=lcon) {
lcon=con;
con.rollback();
throw new SQLException("Transaction was started with another connection");
}
con.commit();
} catch (SQLException e) { quentinc.util.Misc.throwUncheckedException(e); }
}

public <T extends Record> boolean delete (T obj) {
if (obj==null) return true;
String str = deleteStatements.get(obj.getClass());
if (str==null) {
str = obj.generateDeleteStatement();
deleteStatements.put(obj.getClass(), str);
}
int recursion = 0;
while (++recursion<3) try {
PreparedStatement p = getPreparedStatement(str);
int n=0;
for (Field f : obj.getClass().getDeclaredFields()) {
if (!obj.isSQLPrimaryKey(f)) continue;
f.setAccessible(true);
p.setObject(++n, f.get(obj));
}
return p.executeUpdate() >0;
} 
catch (SQLException e) { close(); }
catch (IllegalAccessException e) { quentinc.util.Misc.throwUncheckedException(e); }
return false;
}

public <T extends Record> T fetch (ResultSet rs, Class<T> c) throws IllegalAccessException, InstantiationException, SQLException {
if (rs==null || rs.isClosed() || !rs.next()) return null;
T obj = c.newInstance();
obj.update(rs);
return obj;
}

public <T extends Record, C extends Collection<T>> C fetchAll (ResultSet rs, Class<T> c, C col) throws InstantiationException, IllegalAccessException, SQLException {
T obj = null;
while ((obj = fetch(rs, c))!=null) col.add(obj);
return col;
}
public <T extends Record, C extends Collection<T>> C executeSelect (Class<T> c, C col, String sql, Object... args) {
ResultSet rs = null;
try {
rs = executeQuery(sql, args);
return fetchAll(rs, c, col);
} 
catch (SQLException e) { quentinc.util.Misc.throwUncheckedException(e); }
catch (InstantiationException e) { quentinc.util.Misc.throwUncheckedException(e); }
catch (IllegalAccessException e) { quentinc.util.Misc.throwUncheckedException(e); }
finally { try { if (rs!=null) rs.close(); } catch (SQLException e) {}}
return col;
}

public <T extends Record> T executeSelect (Class<T> c, String sql, Object... args) {
ResultSet rs = null;
try {
rs = executeQuery(sql, args);
return fetch(rs, c);
} 
catch (SQLException e) { quentinc.util.Misc.throwUncheckedException(e); }
catch (InstantiationException e) { quentinc.util.Misc.throwUncheckedException(e); }
catch (IllegalAccessException e) { quentinc.util.Misc.throwUncheckedException(e); }
finally { try { if (rs!=null) rs.close(); } catch (SQLException e) {}}
return null;
}

public <T extends Record> boolean save (T obj) {
if (obj==null) return true;
String str = saveStatements.get(obj.getClass());
if (str==null) {
str = obj.generateSaveStatement(1);
saveStatements.put(obj.getClass(), str);
}
int recursion = 0;
while (++recursion<3) try {
PreparedStatement p = getPreparedStatement(str);
int n=0;
for (Field f : obj.getClass().getDeclaredFields()) {
if (!obj.isSQLField(f)) continue;
f.setAccessible(true);
p.setObject(++n, f.get(obj));
}
p.executeUpdate();
ResultSet rs = p.getGeneratedKeys();
obj.updatePrimaryKeys(rs);
} 
catch (SQLException e) { 
close(); 
if (recursion>=3) quentinc.util.Misc.throwUncheckedException(e);
}
catch (IllegalAccessException e) { quentinc.util.Misc.throwUncheckedException(e); }
return false;
}

public <T extends Record> boolean deleteAll (Collection<T> objs) {
boolean re=true;
for (T obj : objs) re = re && delete(obj);
return re;
}
public <T extends Record> boolean deleteAll (T... objs) {
boolean re=true;
for (T obj : objs) re = re && delete(obj);
return re;
}

public <T extends Record> boolean saveAll (T... objs) { return saveAll(Arrays.asList(objs)); }
public <T extends Record> boolean saveAll (Collection<T> objs) {
if (objs==null || objs.size()<=0) return true;
else if (objs.size()==1) return save(objs.iterator().next());
int recursion = 0;
String str = null;
Field[] fields = null;
PreparedStatement p = null;
while (++recursion<3) try {
int n=0;
for (T obj: objs) {
if (fields==null) fields = obj.getClass().getDeclaredFields();
if (str==null) str = obj.generateSaveStatement(objs.size());
if (p==null) p = getConnection().prepareStatement(str);
for (Field f : fields) {
if (!obj.isSQLField(f)) continue;
f.setAccessible(true);
p.setObject(++n, f.get(obj));
}}
return p.executeUpdate() >0;
} 
catch (SQLException e) { 
p=null; 
close(); 
if (recursion>=3) quentinc.util.Misc.throwUncheckedException(e);
}
catch (IllegalAccessException e) { quentinc.util.Misc.throwUncheckedException(e); }
finally { try { if (p!=null) p.close(); } catch (SQLException e) {}}
return false;
}

}
