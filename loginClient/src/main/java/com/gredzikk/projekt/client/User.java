package com.gredzikk.projekt.client;

import java.net.Socket;

public class User
{
    String login;
    String password;
    String hashedPassword;
    Socket socket;
    int taskCount;

    String lastLogin;

    public User(String login, String lastLogin) {
        this.login = login;
        this.lastLogin = lastLogin;
    }

    public User(String login)
    {
        this.login = login;
    }

    public User(){
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String last){
        this.lastLogin = last;
    }
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

}
