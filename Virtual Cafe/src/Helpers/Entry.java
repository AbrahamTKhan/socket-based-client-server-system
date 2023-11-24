package Helpers;

public class Entry { //Class for every order created by a customer
    private int id;
    private String type;
    private boolean active;
    public Entry (int id, String type, boolean active)
    {
        this.id = id;
        this.type = type;
        this.active = active;
    }

    public int getID()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public void setID(int id) { this.id = id; }

    public boolean getActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}
