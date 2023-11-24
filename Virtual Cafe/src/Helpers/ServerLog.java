package Helpers;

import java.util.ArrayList;
import java.util.List;

public class ServerLog { //Class for every time the server logs a change
    List<String[]> Logs = new ArrayList<>();

    public void addLog (String dtf, int numCustomers, int numWaiting, int teaW, int coffW, int teaB, int coffB, int teaT, int coffT)
    {
        String[] log = new String[6];
        log[0] = "Date and Time: " + dtf;
        log[1] = "Number of customers in the cafe: " + numCustomers;
        log[2] = "Number of customers waiting for orders: " + numWaiting;
        log[3] = "Waiting area: " + teaW + " Tea(s) and " + coffW + " Coffee(s)";
        log[4] = "Brewing area: " + teaB + " Tea(s) and " + coffB + " Coffee(s)";
        log[5] = "Tray area: " + teaT + " Tea(s) and " + coffT + " Coffee(s)";

        Logs.add(log);
    }
}
