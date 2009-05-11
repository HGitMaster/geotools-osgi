import java.sql.*;

import SQLite.Callback;
import SQLite.Database;
import SQLite.JDBC2y.JDBCConnection;

public class Main {

  public static void main( String[] args ) throws Exception {

    Class.forName( "SQLite.JDBCDriver");
    Connection cx = DriverManager.getConnection("jdbc:sqlite:/foo");

    Statement st = cx.createStatement();
    ResultSet rs = st.executeQuery("SELECT * FROM t1 WHERE 1=0");
    ResultSetMetaData md = rs.getMetaData();
    System.out.println(md.getColumnCount());
    System.out.println(md.getColumnName(1));

    rs.close();
    st.close();
  }
}

