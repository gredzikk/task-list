import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class NewConnectionHandler
{
    public boolean authUser(Socket socket) throws IOException, SQLException, ClassNotFoundException
    {
        boolean authSuccess = false;
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/projekt",
                "serverAdmin", "serverAdmin1");

        BufferedReader clientToServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter serverToClient = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        Statement statement;
        ResultSet resultSet;
        String login;
        String password;

        String cr = clientToServer.readLine();
        JSONObject clientToServerData = new JSONObject(cr);
        login = clientToServerData.getString("login");
        password = clientToServerData.getString("password");
//        System.out.println("SERVER: Received login of " + login + ", pass of " + password);

        JSONObject serverToClientData = new JSONObject();
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT login FROM users WHERE login = '" + login + "'");
        if (!resultSet.next()) {
            System.out.println("SERVER: New user, creating account");
            PreparedStatement st = connection.prepareStatement("INSERT INTO users (login, password, creation_date, last_login) VALUES (?, ?, ?, ?)");
            st.setString(1, login);
            st.setString(2, password);
            st.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            st.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            st.executeUpdate();
            st.close();

            String newusr = "CREATE USER " + login + " WITH PASSWORD '" + password + "'";
            PreparedStatement newuser = connection.prepareStatement(newusr);
            System.out.println("SERVER: Creating new db user with login: " + login + ", password: " + password);
            newuser.execute();
            newuser.close();

            String createTable = "create table " + login + "_tasks (" +
                    " name varchar(100) primary key, " +
                    " note text, " +
                    " done boolean not null)";

            PreparedStatement createtable = connection.prepareStatement(createTable);
            createtable.execute();

            String table = "INSERT INTO " + login + "_tasks (name, note, done) VALUES (?, ?, ?)";
            createtable = connection.prepareStatement(table);

            // Step 3: Set the parameter values for each row to be inserted
            createtable.setString(1, "Powitanie");
            createtable.setString(2, "Witaj użytkowniku. To jest lista twoich zadań.");
            createtable.setBoolean(3, false);
            createtable.addBatch();

            createtable.setString(1, "Możliwości");
            createtable.setString(2, "Na tej liście wyświetlają się nazwy zadań, ich opisy oraz ich aktualny stan.");
            createtable.setBoolean(3, false);
            createtable.addBatch();

            createtable.setString(1, "Dodawanie");
            createtable.setString(2, "Nowe zadanie możesz dodać wpisując jego nazwę oraz opis w polu powyżej, a następnie klikając przycisk [Dodaj zadanie]");
            createtable.setBoolean(3, false);
            createtable.addBatch();

            createtable.setString(1, "Edycja");
            createtable.setString(2, "Możesz edytować nazwę, opis oraz stan zadania klikając na wybranej wartości.");
            createtable.setBoolean(3, false);
            createtable.addBatch();

            createtable.setString(1, "Usuwanie");
            createtable.setString(2, "Zadanie możesz usunąć zaznaczając je kliknięciem myszy i naciskając przycisk [Usuń zadanie]");
            createtable.setBoolean(3, false);
            createtable.addBatch();

            createtable.executeBatch();
            createtable.close();
            System.out.println("SERVER: Created user table: " + login + "_tasks");

            PreparedStatement privileges = connection.prepareStatement("GRANT ALL ON " + login + "_tasks TO " + login);
            privileges.execute();
            privileges.close();

            authSuccess = true;
            serverToClientData.put("auth", authSuccess);
        }
        else
        {
            resultSet = statement.executeQuery("SELECT login, password FROM users WHERE login ='" + login + "' AND password = '" + password + "'");
            if(resultSet.next())
            {
                authSuccess = true;
                serverToClientData.put("auth", authSuccess);
//                System.out.println("SERVER: Login successful");
            }
            else
            {
                System.out.println("SERVER: Password mismatch");
                serverToClientData.put("auth", authSuccess);
                serverToClient.write(serverToClientData.toString());
                serverToClient.newLine();
                serverToClient.flush();
                return false;
            }
        }
        serverToClient.write(serverToClientData.toString());
        serverToClient.newLine();
        serverToClient.flush();

        resultSet = statement.executeQuery("SELECT login, last_login FROM users WHERE login = '" + login + "'");
        if (resultSet.next()) {
            LocalDateTime ldt = resultSet.getTimestamp("last_login").toLocalDateTime();
            DateTimeFormatter frmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String tmstmp = ldt.format(frmt);
            System.out.println("SERVER: Welcome [" + login + "], your last login time is " + tmstmp);
            serverToClientData.put("last", tmstmp);
            serverToClient.write(serverToClientData.toString());
            serverToClient.newLine();
            serverToClient.flush();
        }

        PreparedStatement st = connection.prepareStatement("UPDATE users SET last_login = ? WHERE login = ?");
        st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        st.setString(2, login);
        st.executeUpdate();
        st.close();

        return true;
    }
}
