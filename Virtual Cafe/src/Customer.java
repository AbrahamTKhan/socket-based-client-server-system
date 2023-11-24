import Helpers.CustomerAccount;

import java.util.Scanner;

public class Customer {
    public static void main(String[] args) {
        System.out.println("Enter your name:");

        try {
            Scanner in = new Scanner(System.in);
            String name = in.nextLine(); //Takes the first input as the customer's name

            CustomerAccount customerAccount = new CustomerAccount(name); //Creates a customer account with the name provided
            System.out.println("Logged in successfully.");
            System.out.println("\nHi " + name + "!");

            int numOrder1 = 0; //Keeps count of how many teas/coffees are requested
            int numOrder2 = 0;
            while (true) {
                System.out.println("---------------------------------------------------------" +
                        "\n'Order Status' to check your orders." +
                        "\n'Order _ Tea(s)' to order a tea." +
                        "\n'Order _ Coffee(s)' to order a coffee." +
                        "\n'Quit' or Ctrl+C to exit the cafe" +
                        "\nUse 'and' to double order e.g. Order 2 Teas 'and' 4 Coffees" +
                        "\n---------------------------------------------------------");

                String choice = in.nextLine().trim().toLowerCase();
                String[] choiceSplit = choice.split(" "); //Breaks up input for processing

                if (choice.equals("quit")) //Terminates program
                {
                    System.exit(0);
                }
                else if (!choiceSplit[0].equals("order"))
                {
                    System.out.println("Unknown command: " + choiceSplit[0]);
                    continue;
                }
                else if (choice.equals("order status")) //Returns the status of the customer's order(s)
                {
                    customerAccount.getOrders();
                }
                else
                {
                    if (choiceSplit.length == 3) //If the customer orders 1 type of drink (tea or coffee)
                    {
                        try { numOrder1 = Integer.parseInt(choiceSplit[1]); } //Evaluates drink 'amount' input
                        catch (Exception e)
                        {
                            System.out.println("\nError | Unknown command: " + choiceSplit[1] + "\n");
                            continue;
                        }

                        if (choiceSplit[2].equals("tea") || choiceSplit[2].equals("teas")) //Evaluates whether the customer wants a tea or coffee
                        {
                            customerAccount.addTea(numOrder1);
                        }
                        else if (choiceSplit[2].equals("coffee") || choiceSplit[2].equals("coffees"))
                        {
                            customerAccount.addCoffee(numOrder1);
                        }
                        else
                        {
                            System.out.println("\nError | Unknown command: " + choiceSplit[2] + "\n");
                            continue;
                        }
                    }

                    else if (choiceSplit.length == 6 && choiceSplit[3].equals("and")) //If the customer's order includes both tea(s) and coffee(s)
                    {
                        try
                        {
                            numOrder1 = Integer.parseInt(choiceSplit[1]); //Evaluates 2 drink 'amount' input
                            numOrder2 = Integer.parseInt(choiceSplit[4]);
                        }
                        catch (Exception e)
                        {
                            System.out.println("\nError | Unknown command: " + choice + "\nExpected integer before order type\n");
                            continue;
                        }

                        if (choiceSplit[2].equals("tea") || choiceSplit[2].equals("teas") && choiceSplit[5].equals("coffee") || choiceSplit[5].equals("coffees"))
                        {
                            customerAccount.doubleOrder(numOrder1, numOrder2); //Correctly orders tea and coffee inputs based on the order that they were entered
                        }
                        else if (choiceSplit[2].equals("coffee") || choiceSplit[2].equals("coffees") && choiceSplit[5].equals("tea") || choiceSplit[5].equals("teas"))
                        {
                            customerAccount.doubleOrder(numOrder2, numOrder1);
                        }
                        else {
                            System.out.println("\nError | Unknown order type entered: " + choiceSplit[2] + " & " + choiceSplit[5] + "\n");
                            continue;
                        }
                    }

                    else
                    {
                        System.out.println("\nError | Unknown command: " + choice + "\n");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
