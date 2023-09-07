import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MainServer
{
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        initDatabase();

        ArrayList<ServerThread> threadList = new ArrayList<>();
        try (ServerSocket serversocket = new ServerSocket(5000)) {
            System.out.println("SERVER: Init");
            while (true) {
                Socket clientSocket = serversocket.accept();
                ServerThread serverThread = new ServerThread(clientSocket, threadList);
                threadList.add(serverThread);
                serverThread.start();
            }
        } catch (Exception e) {
            System.out.println("SERVER: Error occured in main: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public static void initDatabase() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/projekt",
                "serverAdmin", "serverAdmin1");
        DatabaseMetaData dbm = connection.getMetaData();
        // sprawdz czy istnieje tabela users
        ResultSet tables = dbm.getTables(null, null, "users", null);
        if (tables.next()) {
            System.out.println("SERVER: Do you want to clean database? [y/n]");
            Scanner input = new Scanner(System.in);
            String choice = input.nextLine();
            if(choice.charAt(0) == 'y') {
                wipeDatabase();
            }
        }
        else {
            System.out.println("SERVER: Table 'users' doesnt exist");
            PreparedStatement create = connection.prepareStatement("create table users (" +
                    "login varchar(50) primary key, " +
                    "password varchar (64) not null, " +
                    "creation_date timestamp not null, " +
                    "last_login timestamp not null " +
                    ")");
            create.execute();
            create.close();
            System.out.println("SERVER: Table 'users' created");
        }
        connection.close();

    }

    public static void wipeDatabase() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/projekt",
                "serverAdmin", "serverAdmin1");
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM users");
        while(rs.next()) {
            String login = rs.getString("login");
            PreparedStatement ps = connection.prepareStatement("DROP TABLE " + login + "_tasks");
            ps.execute();
            ps = connection.prepareStatement("DROP USER " + login);
            ps.execute();
            ps.close();
        }

        PreparedStatement ad = connection.prepareStatement("DELETE FROM users");
        ad.execute();
        ad.close();
        connection.close();
        System.out.println("SERVER: Database wiped");
    }
}