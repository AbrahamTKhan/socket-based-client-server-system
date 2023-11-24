import Helpers.BaristaProgram;
import Helpers.CustomerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Barista
{
    private final static int port = 8888;
    private static final BaristaProgram BARISTA_PROGRAM = new BaristaProgram();

    public static void main(String[] args)
    {
        RunServer();
    }

    private static void RunServer() {
        ServerSocket serverSocket = null; //Creates an empty listener socket
        try {
            serverSocket = new ServerSocket(port); //Sets the port that customers can connect with
            BARISTA_PROGRAM.writeLog(); //Initial log entry
            while (true) {
                Socket socket = serverSocket.accept(); //Accepts incoming connections
                new Thread(new CustomerHandler(socket, BARISTA_PROGRAM)).start(); //Creates a new thread whenever a connection is made
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
