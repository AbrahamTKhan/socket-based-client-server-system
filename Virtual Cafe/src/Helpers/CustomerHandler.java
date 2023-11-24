package Helpers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class CustomerHandler implements Runnable {
    private final Socket socket;
    private BaristaProgram baristaProgram;

    public CustomerHandler(Socket socket, BaristaProgram baristaProgram) {
        this.socket = socket;
        this.baristaProgram = baristaProgram;
    }

    @Override
    public void run() {
        int customerID = 0;
        try (
                Scanner scanner = new Scanner(socket.getInputStream()); //Creates streams for communication with the customer
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            try {
                String customerName = scanner.nextLine(); //Receives customers name input
                customerID = baristaProgram.newID(); //Generates new ID
                baristaProgram.createAccount(customerID, customerName); //Creates account

                baristaProgram.writeLog();
                writer.println("SUCCESS"); //Confirms successful connection

                boolean run = true;
                while (run) { //Runs until customer disconnects
                    String line = scanner.nextLine();
                    String[] substrings = line.split(" "); //Breaks up input for processing
                    switch (substrings[0].toLowerCase()) {
                        case "orders":
                            int[] orders = baristaProgram.getListOfOrders(customerID);
                            if (orders[0] == -1) //First element is -1 if the customer is idle
                            {
                                writer.println("-1");
                                writer.println("No order found for " + customerName);
                            }
                            else if (orders[0] == -2) //First element is -2 if there are only items in the tray area (orders complete)
                            {
                                writer.println("-2");
                                writer.println("Order delivered to " + customerName + " (" + orders[4] + " teas and " + orders[5] + " coffees)");
                            }
                            else //Returns all orders if there are any still be processed
                            {
                                writer.println(orders[0]);
                                String orderMessage = "Order status for " + customerName + ": \n";
                                if (orders[0] == 0 && orders[1] == 0)
                                {
                                    orderMessage += "-\n";
                                }
                                else
                                {
                                    orderMessage += "- " + orders[0] + " tea(s) and " + orders[1] + " coffee(s) in the waiting area.\n";
                                }
                                if (orders[2] == 0 && orders[3] == 0)
                                {
                                    orderMessage += "-\n";
                                }
                                else
                                {
                                    orderMessage += "- " + orders[2] + " tea(s) and " + orders[3] + " coffee(s) in the brewing area.\n";
                                }
                                if (orders[4] == 0 && orders[5] == 0)
                                {
                                    orderMessage += "-";
                                }
                                else
                                {
                                    orderMessage += "- " + + orders[4] + " tea(s) and " + orders[5] + " coffee(s) in the tray area.";
                                }
                                writer.println(orderMessage);
                            }
                            break;

                        case "add_tea": //Creates tea order
                            int teaNum = Integer.parseInt(substrings[1]);
                            baristaProgram.addTea(customerID, teaNum);
                            writer.println("order received for " + customerName + " (" + teaNum + " tea(s))");
                            break;

                        case "add_coffee": //Creates coffee order
                            int coffeeNum = Integer.parseInt(substrings[1]);
                            baristaProgram.addCoffee(customerID, coffeeNum);
                            writer.println("order received for " + customerName + " (" + coffeeNum + " coffee(s))");
                            break;

                        case "double_order": //Creates order with both tea and coffee
                            int teaAmount = Integer.parseInt(substrings[1]);
                            int coffeeAmount = Integer.parseInt(substrings[2]);

                            baristaProgram.addTea(customerID, teaAmount);
                            baristaProgram.addCoffee(customerID, coffeeAmount);

                            writer.println("order received for " + customerName + " (" + teaAmount + " tea(s) and " + coffeeAmount + " coffees)");
                            break;

                        case "repurpose": //Attempts to repurpose orders when customer leaves
                            run = false;
                            baristaProgram.repurposeOrder(customerID);
                            break;

                        default:
                            throw new Exception("Unknown command: " + substrings[0]);
                    }
                }
            }

            catch (Exception e)
            {
                writer.println("ERROR " + e.getMessage());
                e.printStackTrace();
                socket.close();
            }
        }

        catch (Exception e)
        {
            System.out.println("Streams failed");
            e.printStackTrace();
        }

        finally //Final processing when the customer leaves
        {
            baristaProgram.customerLeft(customerID);
            try { baristaProgram.writeLog(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}
