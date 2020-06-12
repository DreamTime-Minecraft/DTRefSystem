package su.dreamtime.dtrefsystem.data;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Random;

public class Database implements AutoCloseable
{
    private Connection connection;
    private String host;
    private int port;
    private String login;
    private String password;
    private String database;
    private String argString;
    /**
     *
     * @param args  format: "arg=value"
     */
    public Database(String host, int port, String login, String password, String database,  String... args)
    {
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        this.database = database;
        StringBuilder argSB = new StringBuilder("");
        for (String arg : args)
        {
            argSB.append("&").append(arg);
        }
        this.argString = argSB.toString();
        checkConnection();
    }
    public void checkConnection()
    {
        try {
            if (connection == null || !connection.isValid(10))
            {
                if (connection != null) connection.close();
                this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true" + argString, login, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public int execute(String sql, Object... args)
    {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            int i = 1;
            for (Object arg : args)
                statement.setObject(i++, arg);
            return statement.executeUpdate();
        } catch (NullPointerException | SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ResultSet query(String sql, Object... args) throws SQLException
    {
        checkConnection();
        long time = System.currentTimeMillis();
        PreparedStatement stmt = connection.prepareStatement(sql);
        int i = 1;
        for (Object arg : args)
        {
            stmt.setObject(i++, arg);
        }

        stmt.closeOnCompletion();
        return stmt.executeQuery();
    }
    private static Random random = new Random();
    public static String rndString(int length)
    {
        String generateFrom = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder rndString = new StringBuilder(length);

        for (int i = 0; i < length; i++)
        {
            int rndInt = random.nextInt(generateFrom.length());

            rndString.append(generateFrom.charAt(rndInt));
        }

        return rndString.toString();
    }

    public static String md5(String str)
    {
        String md5string = null;
        try
        {
            byte[] bytes = str.getBytes("UTF-8");
            byte[] md5 = MessageDigest.getInstance("MD5").digest(bytes);
            BigInteger bigInt = new BigInteger(1, md5);

            md5string  = bigInt.toString(16);
        }
        catch (UnsupportedEncodingException | NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return md5string;
    }

    @Override
    public void close() throws Exception
    {
        if (connection != null)
            connection.close();
    }

    public Connection getConnection() {
        return connection;
    }
}
