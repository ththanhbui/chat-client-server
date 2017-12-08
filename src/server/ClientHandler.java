package server;

import messages.*;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class ClientHandler {
    private Socket socket;
    private MultiQueue<Message> multiQueue;
    private String nickname;
    private MessageQueue<Message> clientMessages;
    private Thread fromClient;
    private Thread fromServer;

    public ClientHandler(Socket s, MultiQueue<Message> q) {
        try {
            socket = s;
            multiQueue = q;

            clientMessages = new SafeMessageQueue<>();
            q.register(clientMessages);

            // Generate random nickname
            char[] chars = "0123456789".toCharArray();
            Random rnd = new Random();
            StringBuilder sb = new StringBuilder("Anonymous");
            for (int i = 0; i < 5; i++)
                sb.append(chars[rnd.nextInt(chars.length)]);
            nickname = sb.toString();

            // create a StatusMessage to record the fact that a new client has connected to the Server
            StatusMessage sm =
                    new StatusMessage(String.format("%s connected from %s", nickname, socket.getInetAddress().getHostName()));
            multiQueue.put(sm);

            InputStream in = socket.getInputStream();

            fromClient = new Thread() {
                @Override
                public void run() {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(in);

                        while (true) {
                            Object obj = ois.readObject();

                            if (obj instanceof ChangeNickMessage) {
                                ChangeNickMessage nick = (ChangeNickMessage) obj;
                                StatusMessage sm =
                                        new StatusMessage(String.format("%s is now known as %s.", nickname, nick.name));
                                nickname = nick.name;

                                multiQueue.put(sm);
                            } else if (obj instanceof ChatMessage) {
                                ChatMessage cm = (ChatMessage) obj;
                                RelayMessage rm = new RelayMessage(nickname, cm);

                                multiQueue.put(rm);
                            }
                        }
                    } catch (IOException e) {
                        multiQueue.deregister(clientMessages);
                        StatusMessage sm = new StatusMessage(String.format("%s has disconnected.", nickname));
                        multiQueue.put(sm);
                        fromServer.interrupt();
                    } catch (ClassNotFoundException cnf) {
                        System.err.println("Class not found");
                        cnf.printStackTrace();
                    }
                }
            };

            fromServer = new Thread() {
                @Override
                public void run() {
                    try {
                        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());

                        /**
                         * Sending information of GUIClient class
                         */
                        sendClass(GUIClient.class, os, null);
                        sendClass(GUIClient.class, os, "1");
//                        sendClass(GUIClient.class, os, "2");
//                        sendClass(GUIClient.class, os, "3");

                        /**
                         * Sending information of GUIMessage class
                         */
                        sendClass(GUIMessage.class, os, null);

                        os.writeObject(new GUIMessage());
                        os.flush();

                        while (true) {
                            Message m = clientMessages.take();
                            os.writeObject(m);
                            os.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            fromClient.setDaemon(true);
            fromClient.start();
            fromServer.setDaemon(true);
            fromServer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendClass(Class<?> clazz, ObjectOutputStream oos, String innerClass) {
        try {
            // Get the name of the class
            String className = clazz.getName();
            if (innerClass != null) {
                className += "$" + innerClass;
            }

            // Convert to a full path
            String path = clazz.getClassLoader().getResource(className.replaceAll("\\.", "/") + ".class").getPath();
            File f = new File(path);
            byte[] data = new byte[(int) f.length()];
            new FileInputStream(f).read(data);

            // Send the definition of the class
            NewMessageType msg = new NewMessageType(className, data);
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

