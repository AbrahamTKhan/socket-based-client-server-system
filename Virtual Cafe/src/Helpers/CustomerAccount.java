package Helpers;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class CustomerAccount{
    final int port = 8888;
    private final Scanner reader;
    private final PrintWriter writer;

    public CustomerAccount(String name) throws Exception {
        Socket socket = new Socket("localhost", port); //Creates socket that connects to the server

        reader = new Scanner(socket.getInputStream()); //Gets server's input with scanner
        writer = new PrintWriter(socket.getOutputStream(), true); //Uses output stream to send data to the server

        Runtime.getRuntime().addShutdownHook(new Thread(() -> { //Creates thread to handle SIGTERM signals
            try
            {
                writer.println("REPURPOSE"); //Notifies server of customer disconnect
                socket.close();
                System.out.println("Leaving cafe");
                reader.close();
                System.out.println("Closing reader");
                writer.close();
                System.out.println("Closing writer");
            }
            catch (Exception e) { System.out.println(e); };
        }));


        writer.println(name); //Sends the customer's name to the server


        String line = reader.nextLine();
        if (line.trim().compareToIgnoreCase("success") != 0) //Throws exception if connection wasn't successful
        {
            throw new Exception(line);
        }
    }

    public void getOrders() { //Returns the customer's order status
        writer.println("ORDERS");

        String line = reader.nextLine();
        if (line.equals("-1") || line.equals("-2")) //-1 indicates that they are idle and not waiting for an order
        {                                           //-2 indicates that their order is complete
            System.out.println(reader.nextLine());  //Only has to parse 1 line for this response
        }
        else if (Integer.parseInt(line) >= 0) //Prints the order statuses for the waiting, brewing and tray areas
        {                                     //Only if there are orders currently being processed
            System.out.println(reader.nextLine());
            System.out.println(reader.nextLine());
            System.out.println(reader.nextLine());
            System.out.println(reader.nextLine());
        }

    }

    public void addTea(int amount) { //Tells the server to order a certain number of teas
        writer.println("ADD_TEA " + amount);
        System.out.println(reader.nextLine());
    }

    public void addCoffee(int amount) { //Tells the server to order a certain number of coffees
        writer.println("ADD_COFFEE " + amount);
        System.out.println(reader.nextLine());
    }

    public void doubleOrder(int teaAmount, int coffeeAmmount) //Orders tea(s) and coffee(s) at the same time
    {
        writer.println("DOUBLE_ORDER " + teaAmount + " " + coffeeAmmount);
        System.out.println(reader.nextLine());
    }
}
