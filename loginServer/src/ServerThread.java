import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;


public class ServerThread extends Thread
{
    private final Socket clientSocket;
    private ArrayList<ServerThread> threadList;
    boolean auth = false;

    public ServerThread(Socket socket, ArrayList<ServerThread> threads) {
        this.clientSocket = socket;
        this.threadList = threads;
    }

    @Override
    public void run() {
        try {
            System.out.println("SERVER: New client connected");
            NewConnectionHandler nch = new NewConnectionHandler();
            auth = nch.authUser(clientSocket);

            if (auth)
            {
                while(true) {
                    BufferedReader clientToServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter serverToClient = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    String cd;
                    while ((cd = clientToServer.readLine()) != null)
                    {
                        JSONObject clientToServerData = new JSONObject(cd);
                        String status = clientToServerData.getString("status");

                        System.out.println("SERVER: " + status);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("SERVER: Connection lost");
            e.printStackTrace();
        }
        finally {
            try {
                clientSocket.close();
                System.out.println("SERVER: Socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}