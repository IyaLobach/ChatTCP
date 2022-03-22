package com.suai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

//Пользователь может поставить себе будильник (@alarm time) на сервере (класс Timer).
//Когда срабатывает будильник, пользователю присылается сообщение “Wake up!”.
//Все должно работать, если ты отсоединился и присоединился опять.


public class TCPServer {

  private ServerSocket serverSocket;
  private ArrayList<ClientThread> clients; // список всех клиентов ддля отправки
  private int number = 1; // число подключений

  public TCPServer() {
    try {
      serverSocket = new ServerSocket(9876);
      clients = new ArrayList<>();
      System.out.println("Server waiting for clients to connect");
      while (true) {
        Socket clientSocket = serverSocket.accept();
        ClientThread clientThread = new ClientThread(clientSocket);
        clients.add(clientThread);
        clientThread.start();
      }
    } catch (Exception exception) {
      System.out.println(exception.getMessage());
      exception.printStackTrace();
    }
  }

  public synchronized void increment(){
    number++;
  }

  public class ClientThread extends Thread {

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;
    private boolean isLeft = false;
    private int Id;


    public ClientThread(Socket cs) {
      try {
        System.out.println("New connection");
        clientSocket = cs;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        clientName = "Anon" + number;
      } catch (Exception exception) {
        System.out.println(exception.getMessage());
        exception.printStackTrace();
      }
    }

    private void send(StringBuilder message) {
      for (int i = 0; i < clients.size(); i++) {
        if (clients.get(i) != this) {
          clients.get(i).out.println(message.toString());
        }
      }
    }

    private boolean correctTime(int year, int month, int date, int hours, int min, int sec) {
      if (year <= 0) {
        return false;
      }
      if (month < 0 || month > 11) {
        return false;
      }
      if (date <= 0 || date > 31) {
        return false;
      }
      if (hours <= 0 || hours > 23) {
        return false;
      }
      if (min < 0 || min > 59) {
        return false;
      }
      if (sec < 0 || sec > 59) {
        return false;
      }
      return true;
    }

    public void run() { // поток оправки сообщений с сервера всем клиентам!
      try {
        out.println("If you already have Id type it. If not, then type " + number);
        increment();
        Id = Integer.parseInt(in.readLine());
        out.println("You Id = " + Id);
        while (!isLeft) {
          String fromClient = in.readLine();
          StringBuilder message = new StringBuilder();
          if (fromClient.contains("@name")) {
            message.append(clientName).append(" changed name to ").append(fromClient.substring(6));
            clientName = fromClient.substring(6);
            send(message);
            continue;
          }
          if (fromClient.contains("@senduser")) {
            String[] tmp = fromClient.split(" ");
            message.append(clientName).append(" only for you: ");
            for (int i = 2; i < tmp.length; i++) {
              message.append(tmp[i]).append(" ");
            }
            for (int i = 0; i < clients.size(); i++) {
              if (clients.get(i).clientName.equals(tmp[1])) {
                clients.get(i).out.println(message.toString());
              }
            }
            continue;
          }
          if (fromClient.contains("@alarm")) {
            String[] getDate = fromClient.substring(12).split(" ");
            if (getDate.length != 6) {
              out.println("Incorrect time format");
              continue;
            }
            int year = Integer.parseInt(getDate[0]);
            int month = Integer.parseInt(getDate[1]) - 1;
            int date = Integer.parseInt(getDate[2]);
            int hours = Integer.parseInt(getDate[3]);
            int min = Integer.parseInt(getDate[4]);
            int sec = Integer.parseInt(getDate[5]);
            if (!correctTime(year, month, date, hours, min, sec)) {
              out.println("Incorrect time");
            } else {
              MyTimer t = new MyTimer(year, month, date, hours, min, sec, Id);
            }
            continue;
          }
          if (fromClient.contains("@left")) {
            message.append(clientName).append(": left server");
            send(message);
            for (int i = 0; i < clients.size(); i++) {
              if (clients.get(i).clientName.equals(clientName)) {
                clients.remove(i);
              }
            }
            isLeft = true;
            continue;
          }
          message.append(clientName).append(": ").append(fromClient);
          send(message);
        }
      } catch (Exception exception) {
        System.out.println(exception.getMessage());
        exception.printStackTrace();
      }
    }
  }

  public class MyTimer {

    private Timer timer;
    private int Id;

    public MyTimer(int year, int month, int date, int hrs, int min, int sec, int clientId) {
      Id = clientId;
      timer = new Timer();
      Calendar calendar = Calendar.getInstance();
      calendar.set(year, month, date, hrs, min, sec);
      timer.schedule(new MyTimerTask(), calendar.getTime());
    }

    private class MyTimerTask extends TimerTask {

      public void run() {
        for (int i = 0; i < clients.size(); i++) {
          if (clients.get(i).Id == Id) {
            clients.get(i).out.println("Wake up!");
          }
        }
        timer.cancel();
      }
    }
  }

  public static void main(String[] args) {
    TCPServer server = new TCPServer();
  }
}
