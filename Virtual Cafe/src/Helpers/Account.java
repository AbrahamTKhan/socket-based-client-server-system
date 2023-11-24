package Helpers;

public class Account { //Account class for every customer that joins the server
    private final String name;
    private final int id;
    private int teas;
    private int coffees;
    private boolean idle;

    public Account(String name, int id) {
        this.name = name;
        this.id = id;
        idle = true;
        teas = 0;
        coffees = 0;
    }

    public String getName() {
        return name;
    }

    public int getID() { return id; }

    public boolean getIdle() {
        return idle;
    }

    public int getTeas() {
        return teas;
    }

    public int getCoffees() { return coffees; }

    public void newTea(int amount) { teas += amount; }

    public void newCoffee(int amount) { coffees  += amount; }

    public void setIdle(boolean idle)
    {
        this.idle = idle;
    }
}

