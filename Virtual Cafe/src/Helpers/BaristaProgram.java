package Helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BaristaProgram {
    private final Map<Integer, Account> accounts = new TreeMap<>(); //Stores all customer accounts
    private int idCount = 0;
    private int numCustomers = 0;

    private final Queue<Entry> teaWaitingArea = new LinkedList<Entry>(); //Various processing areas
    private final Queue<Entry> teaBrewingArea = new LinkedList<Entry>();
    private final Queue<Entry> coffeeWaitingArea = new LinkedList<Entry>();
    private final Queue<Entry> coffeeBrewingArea = new LinkedList<Entry>();
    private final List<Entry> trayArea = new ArrayList<Entry>();

    private ServerLog Log = new ServerLog();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public void writeLog() throws IOException //Writes current server state as a log to a JSON file
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"); //Sets the date and time format
        LocalDateTime now = LocalDateTime.now(); //Gets the current date and time

        int waitingCount = 0;
        for (Account a : accounts.values()) //Checks how many customers aren't idle/waiting for orders
        {
            if (!a.getIdle())
            {
                waitingCount++;
            }
        }

        int teaB = (int) teaBrewingArea.stream().filter(Entry::getActive).count(); //Gets how many teas/coffees are in the brewing area
        int coffB = (int) coffeeBrewingArea.stream().filter(Entry::getActive).count();

        int teaT = 0;
        int coffT = 0;
        for (Entry e : trayArea) //Gets how many teas/coffees are in the tray area
        {
            if (e.getType().equals("Tea"))
            {
                teaT++;
            }
            else if (e.getType().equals("Coffee"))
            {
                coffT++;
            }
        }

        String dateTime = dtf.format(now); //Converts the date/time to string

        String stringLog = "Date and Time: " + dateTime + "\nNumber of customers in the cafe: " + numCustomers
                + "\nNumber of customers waiting for orders: " + waitingCount
                + "\nWaiting area: " + teaWaitingArea.size() + " Tea(s) and " + coffeeWaitingArea.size() + " Coffee(s)"
                + "\nBrewing area: " + teaB + " Tea(s) and " + coffB + " Coffee(s)"
                + "\nTray area: " + teaT + " Tea(s) and " + coffT + " Coffee(s)";

        Log.addLog(dateTime, numCustomers, waitingCount, teaWaitingArea.size(), coffeeWaitingArea.size(), teaB, coffB, teaT, coffT); //Adds to the list of server logs
        System.out.println(stringLog);

        Path path = Paths.get("Logs.json");
        BufferedWriter writer = Files.newBufferedWriter(path);
        GSON.toJson(Log, writer); //Writes log object to JSON file
        writer.close();
        System.out.println();
    }

    public int[] getListOfOrders(int id) { //Returns a customer's orders
        if (accounts.get(id).getIdle()) //Returns -1 if the customer is idle
        {
            int[] idle = new int[1];
            idle[0] = -1;
            return idle;
        }
        else
        {
            int teaW = 0; //Counts how many teas/coffees are in each area
            int coffW = 0;
            int teaB = 0;
            int coffB = 0;
            int teaT = 0;
            int coffT = 0;
            for (Entry e : teaWaitingArea)
            {
                if (e != null && e.getID() == id)
                {
                    teaW++;
                }
            }
            for (Entry e : coffeeWaitingArea)
            {
                if (e != null && e.getID() == id)
                {
                    coffW++;
                }
            }
            for (Entry e : teaBrewingArea)
            {
                if (e != null && e.getID() == id)
                {
                    teaB++;
                }
            }
            for (Entry e : coffeeBrewingArea)
            {
                if (e != null && e.getID() == id)
                {
                    coffB++;
                }
            }

            boolean orderComplete = teaW == 0 && teaB == 0 && coffW == 0 && coffB == 0; //Checks if all orders are complete

            for (Iterator<Entry> iterator = trayArea.iterator(); iterator.hasNext();) { //Counts all teas/coffees in the tray area
                Entry e = iterator.next();                                              //Removes the entry if it's being collected by the customer
                if (e != null && e.getID() == id && e.getType().equals("Tea"))
                {
                    teaT++;
                    if (orderComplete)
                    {
                        iterator.remove();
                        accounts.get(id).setIdle(true);
                    }
                }
                else if (e != null && e.getID() == id && e.getType().equals("Coffee"))
                {
                    coffT++;
                    if (orderComplete)
                    {
                        iterator.remove();
                        accounts.get(id).setIdle(true);
                    }

                }
            }

            int[] orders = {teaW, coffW, teaB, coffB, teaT, coffT};

            if (orderComplete) //Returns -2 if all orders are complete
            {
                orders[0] = -2;
            }

            try {
                writeLog();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return orders;
        }

    }

    public void customerLeft(int id) //Removes account when customer leaves
    {
        numCustomers--;
        accounts.remove(id);
    }

    public int newID() //Generates unique ID
    {
        synchronized (accounts)
        {
            int tempID = 0;
            for (Integer customerIDs : accounts.keySet())
            {
                if (tempID < customerIDs)
                {
                    tempID = customerIDs;
                }
            }

            return tempID + 1;
        }
    }

    public void createAccount(int id, String name) { //Creates new account object
        synchronized (accounts) {
            int newID = ++idCount;
            numCustomers++;
            Account account = new Account(name, id);
            accounts.put(newID, account); //Adds to account list
        }
    }

    public void addTea(int id, int amount) //Creates tea order
    {
        accounts.get(id).newTea(amount);
        accounts.get(id).setIdle(false);

        synchronized (teaWaitingArea)
        {
            for (int i = 0; i < amount; i++) //Loops until all orders have been created
            {
                teaWaitingArea.add(new Entry(id, "Tea", true));

            }
            UpdateWaitingArea();
        }

        try {
            writeLog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCoffee(int id, int amount) //Creates coffee order
    {
        accounts.get(id).newCoffee(amount);
        accounts.get(id).setIdle(false);

        synchronized (coffeeWaitingArea)
        {
            for (int i = 0; i < amount; i++) //Loops until all orders have been created
            {
                coffeeWaitingArea.add(new Entry(id, "Coffee", true));

            }
            UpdateWaitingArea();
        }

        try {
            writeLog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void UpdateWaitingArea() //Checks if waiting area entries can be moved to the brewing area
    {
        int teaCount = Math.min(teaWaitingArea.size(), 2); //Limits number of movable entries to 2 (processing limit)
        int coffeeCount = Math.min(coffeeWaitingArea.size(), 2);

        synchronized (teaBrewingArea) {
            while (teaBrewingArea.size() < 2 && teaCount > 0)
            {
                try {
                    writeLog();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                teaCount--;
                teaBrewingArea.add(teaWaitingArea.poll()); //Takes head of waiting area queue and moves it to brewing area

                new Thread(() -> { //Creates new thread with 30 second delay to move to tray area
                    try {
                        Thread.sleep(30000);
                        UpdateBrewingArea("Tea");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }

        synchronized (coffeeBrewingArea) {
            while (coffeeBrewingArea.size() < 2 && coffeeCount > 0)
            {
                try {
                    writeLog();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                coffeeBrewingArea.add(coffeeWaitingArea.poll()); //Takes head of waiting area queue and moves it to brewing area
                coffeeCount--;

                new Thread(() -> { //Creates new thread with 30 second delay to move to tray area
                    try {
                        Thread.sleep(45000);
                        UpdateBrewingArea("Coffee");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

            }
        }

    }

    public void UpdateBrewingArea(String type) //Called when brewing area entry can be moved to tray area
    {
        synchronized (trayArea)
        {
            teaBrewingArea.removeIf(e -> e == null || !e.getActive()); //Remove any entries that are linked to disconnected accounts
            coffeeBrewingArea.removeIf(e -> e == null || !e.getActive());

            if (type.equals("Tea") && teaBrewingArea.peek() != null && teaBrewingArea.peek().getActive()) //Takes head of brewing area queue and moves it to tray area
            {
                trayArea.add(teaBrewingArea.poll());
            }
            else if (type.equals("Coffee") && coffeeBrewingArea.peek() != null && coffeeBrewingArea.peek().getActive())
            {
                trayArea.add(coffeeBrewingArea.poll());
            }

            UpdateWaitingArea();

            try {
                writeLog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void repurposeOrder(int id) //Checks if any entries can be repurposed after a customer leaves
    {
        teaWaitingArea.removeIf(e -> e != null && e.getID() == id); //Removes all waiting area entries related to the disconnected account
        coffeeWaitingArea.removeIf(e -> e != null && e.getID() == id);

        List<Entry> repTrayTea = new ArrayList<>(); //Finds all the tray area entries related to the disconnected account
        List<Entry> repTrayCoffee = new ArrayList<>();
        for (Entry e : trayArea)
        {
            if (e.getID() == id && e.getType().equals("Tea"))
            {
                repTrayTea.add(e);
            }
            else if (e.getID() == id && e.getType().equals("Coffee"))
            {
                repTrayCoffee.add(e);
            }
        }

        int trayTeaCount = repTrayTea.size();
        int trayCoffeeCount = repTrayCoffee.size();

        List<Entry> repBrewTea = new ArrayList<>(); //Finds all the brewing area entries related to the disconnected account
        List<Entry> repBrewCoffee = new ArrayList<>();
        for (Entry e : teaBrewingArea)
        {
            if (e.getID() == id)
            {
                repBrewTea.add(e);
            }
        }

        for (Entry e : coffeeBrewingArea)
        {
            if (e.getID() == id)
            {
                repBrewCoffee.add(e);
            }
        }

        int brewTeaCount = repBrewTea.size();
        int brewCoffeeCount = repBrewCoffee.size();

        Iterator<Entry> iterator = teaWaitingArea.iterator();
        while((trayTeaCount > 0 || brewTeaCount > 0) && iterator.hasNext()) //Upgrades any teas in the waiting area with repurposed items that are more convenient
        {
            Entry e = iterator.next();
            if (e.getID() != id && trayTeaCount > 0)
            {
                repTrayTea.get(--trayTeaCount).setID(e.getID()); //Replaces waiting area item with tray area item
                iterator.remove();
            }
            else if (e.getID() != id && trayTeaCount == 0 && brewTeaCount > 0) //Prioritises repurposed tray area items over brewing area items
            {
                repBrewTea.get(--brewTeaCount).setID(e.getID()); //Replaces waiting area item with brewing area item
                iterator.remove();
            }
        }

        iterator = coffeeWaitingArea.iterator();
        while((trayCoffeeCount > 0 || brewCoffeeCount > 0) && iterator.hasNext()) //Upgrades any coffees in the waiting area with repurposed items that are more convenient
        {
            Entry e = iterator.next();
            if (e.getID() != id && trayCoffeeCount > 0)
            {
                repTrayCoffee.get(--trayCoffeeCount).setID(e.getID()); //Replaces waiting area item with tray area item
                iterator.remove();
            }
            else if (e.getID() != id && trayCoffeeCount == 0 && brewCoffeeCount > 0) //Prioritises repurposed tray area items over brewing area items
            {
                repBrewCoffee.get(--brewCoffeeCount).setID(e.getID()); //Replaces waiting area item with brewing area item
                iterator.remove();
            }
        }

        iterator = teaBrewingArea.iterator();
        while (trayTeaCount > 0 && iterator.hasNext()) //If there are any tray items left to be repurposed, they are used on brewing area items
        {
            Entry e = iterator.next();
            if (e.getID() != id)
            {
                repTrayTea.get(--trayTeaCount).setID(e.getID()); //Replaces brewing area item with tray area item
                trayTeaCount--;
                iterator.remove();
            }
        }

        iterator = coffeeBrewingArea.iterator();
        while (trayCoffeeCount > 0 && iterator.hasNext())
        {
            Entry e = iterator.next();
            if (e.getID() != id)
            {
                repTrayCoffee.get(--trayCoffeeCount).setID(e.getID()); //Replaces brewing area item with tray area item
                iterator.remove();
            }
        }

        for (Entry e : teaBrewingArea) { //All remaining brewing area entries are set to false and deleted later on
            if (e.getID() == id)
            {
                e.setActive(false);
            }
        }

        for (Entry e : coffeeBrewingArea) {
            if (e.getID() == id)
            {
                e.setActive(false);
            }
        }

        trayArea.removeIf(e -> e != null && e.getID() == id); //All remaining tray area entries are deleted

        try {
            writeLog();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UpdateWaitingArea();
    }
}
