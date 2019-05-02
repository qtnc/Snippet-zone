package quentinc.sql;
import java.sql.*;
import java.lang.reflect.*;

public class Record {
public Record () {}
protected void update (ResultSet rs) {
try {
if (rs==null || rs.isClosed()) return;
for (Field f : getClass().getDeclaredFields()) {
if (!isSQLField(f)) continue;
f.setAccessible(true);
f.set(this, rs.getObject(getSQLColumnName(f)));
}
} 
catch (SQLException e) { quentinc.util.Misc.throwUncheckedException(e); }
catch (IllegalAccessException e) { quentinc.util.Misc.throwUncheckedException(e); }
}
protected void updatePrimaryKeys (ResultSet rs) {
try {
if (rs==null || rs.isClosed()) return;
int n=0;
for (Field f : getClass().getDeclaredFields()) {
if (!isSQLPrimaryKey(f)) continue;
f.setAccessible(true);
f.set(this, rs.getObject(++n));
}
} 
catch (SQLException e) { quentinc.util.Misc.throwUncheckedException(e); }
catch (IllegalAccessException e) { quentinc.util.Misc.throwUncheckedException(e); }
}
public boolean save () { return DatabaseConnection.getDefaultConnection().save(this); }
public boolean delete () { return DatabaseConnection.getDefaultConnection().delete(this); }
protected final String generateSaveStatement (int count) {
StringBuilder sb = new StringBuilder(), sb1 = new StringBuilder(), sb2 = new StringBuilder();
for (Field f : getClass().getDeclaredFields()) {
if (!isSQLField(f)) continue;
if (sb1.length()>0) sb1.append(", ");
if (sb2.length()>0) sb2.append(", ");
sb1.append(getSQLColumnName(f));
sb2.append('?');
}
sb.append("REPLACE INTO ")
.append(getSQLTableName())
.append(" (")
.append(sb1)
.append(") VALUES ");
for (int i=0; i<count; i++) {
if (i>0) sb.append(',');
sb.append('(').append(sb2).append(')');
}
sb.append(';');
return sb.toString();
}
protected final String generateDeleteStatement () {
int count = 0;
StringBuilder sb = new StringBuilder();
sb.append("DELETE FROM ").append(getSQLTableName()).append(" WHERE ");
for (Field f : getClass().getDeclaredFields()) {
if (!isSQLPrimaryKey(f)) continue;
if (count++ >0) sb.append(" AND ");
sb.append(getSQLColumnName(f)).append(" = ?");
}
sb.append(';');
return sb.toString();
}


protected boolean isSQLField (Field f) { 
return !Modifier.isTransient(f.getModifiers()) 
|| getClass().isAnnotationPresent(SQLIgnore.class) != f.isAnnotationPresent(SQLIgnore.class);
}
protected boolean isSQLPrimaryKey (Field f) { 
return f.getName().equalsIgnoreCase("id")
|| f.isAnnotationPresent(SQLPrimaryKey.class);
}
protected String getSQLColumnName (Field f) { 
SQLName name = f.getAnnotation(SQLName.class);
return name==null? f.getName() : name.value();
}
@SuppressWarnings("unchecked") protected String getSQLTableName () { 
Class c = getClass();
SQLName name = (SQLName)(c.getAnnotation(SQLName.class));
return name==null? c.getSimpleName() : name.value();
}

}
