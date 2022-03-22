package com.suai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {

  private Socket client;
  private BufferedReader inFromServer;
  private BufferedReader inFromClient;
  private PrintWriter out;

  public TCPClient() {
    try {
      client = new Socket("localhost", 9876);
      System.out.println("The connection to the server has been established");
      inFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
      inFromClient = new BufferedReader(new InputStreamReader(System.in));
      out = new PrintWriter(client.getOutputStream(), true);
      GetFromClient getFromClient = new GetFromClient();
      GetFromServer getFromServer = new GetFromServer();
      getFromClient.start();
      getFromServer.start();

    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public class GetFromServer extends Thread {

    public void run() {
      try {
        while (true) {
          String message = inFromServer.readLine();
          System.out.println(message);
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  public class GetFromClient extends Thread {

    public void run() {
      try {
        System.out.println("You can type:");
        System.out.println("@senduser Username message to send message to a specific user");
        System.out.println("@name to change name");
        System.out.println("@alarm time to set the alarm");
        System.out.println("@left to left server");
        while (true) {
          String fromClient = inFromClient.readLine();
          out.println(fromClient);
          if (fromClient.equals("@left"))
            System.exit(0);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    TCPClient client = new TCPClient();
  }
}
