package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    public static void main(String args[]) {
        try {
            int port = Integer.parseInt(args[0]);

            final ServerSocket ss = new ServerSocket(port);
            final MultiQueue mq = new MultiQueue<>();

            while (true) {
                Socket s = ss.accept();
                ClientHandler ch = new ClientHandler(s, mq);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("Usage: java ChatServer <port>");
            return;
        } catch (IOException e) {
            System.err.println(String.format("Cannot use port number <%s>", args[0]));
            return;
        }

    }
}