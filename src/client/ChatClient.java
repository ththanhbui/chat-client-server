package client;

import messages.*;
import messages.DynamicObjectInputStream;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClient {
    public static void main(String[] args) {
        try {
            // parse and decode server and port numbers from "args"
            String server = args[0];
            int port = Integer.parseInt(args[1]);

            // connect to "server" on "port"
            final Socket s = new Socket(server, port);

            Date connectTime = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            System.out.println(sdf.format(connectTime) + " [Client] " + "Connected to " + server + " on port " + port + ".");

            InputStream in = s.getInputStream();

            Thread output = new Thread() {
                @Override
                public void run() {
                    try {
                        DynamicObjectInputStream ois = new DynamicObjectInputStream(in);
                        while (true) {

                            Object obj = ois.readObject();

                            if (obj instanceof RelayMessage) {
                                RelayMessage rm = (RelayMessage) obj;
                                Date time = rm.getCreationTime();
                                System.out.println(sdf.format(time) + " [" + rm.getFrom() + "] " + rm.getMessage());
                            } else if (obj instanceof StatusMessage) {
                                StatusMessage sm = (StatusMessage) obj;
                                Date time = sm.getCreationTime();
                                System.out.println(sdf.format(time) + " [Server] " + sm.getMessage());
                            } else if (obj instanceof NewMessageType) {
                                NewMessageType nmt = (NewMessageType) obj;
                                ois.addClass(nmt.getName(), nmt.getClassData());
                                System.out.println(sdf.format(connectTime) + " [Client] " + "New class "
                                        + nmt.getName() + " loaded.");
                            } else {
                                Class<?> unknownClass = obj.getClass();
                                Field[] fields = unknownClass.getDeclaredFields();

                                System.out.print(sdf.format(connectTime) + " [Client] " + unknownClass.getSimpleName() + ": ");

                                for (Field f : fields) {
                                    f.setAccessible(true);
                                    if (f.equals(fields[fields.length - 1])) {
                                        System.out.print(f.getName() + "(" + f.get(obj) + ")\n");
                                    } else {
                                        System.out.print(f.getName() + "(" + f.get(obj) + "), ");
                                    }
                                }

                                Method[] methods = unknownClass.getDeclaredMethods();
                                for (Method m : methods) {
                                    m.setAccessible(true);

                                    if (m.getParameterTypes().length == 0 && m.isAnnotationPresent(Execute.class)) {
                                        try {
                                            m.invoke(obj);
                                        } catch (InvocationTargetException ite) {
                                            ite.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException | IllegalAccessException e) {
                        System.out.println("Server has been closed");
                    } catch (ClassNotFoundException cnf) {
                        System.err.println("Class not found");
                    }
                }

            };

            output.setDaemon(true);
            output.start();

            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            String userInput;
            while (true) {
                userInput = r.readLine();
                if (userInput.startsWith("\\nick")) {
                    if (userInput.length() < 7) {
                        System.out.println(sdf.format(connectTime) + " [Client] Please enter a nickname after \\nick.");
                    } else {
                        String nickname = userInput.substring(6);
                        ChangeNickMessage nickName = new ChangeNickMessage(nickname);
                        oos.writeObject(nickName);
                        oos.flush();
                    }
                } else if (userInput.startsWith("\\quit")) {
                    s.close();
                    System.out.println(sdf.format(connectTime) + " [Client] " + "Connection terminated.");
                    break;
                } else if (userInput.startsWith("\\")) {
                    int count = 0;
                    String command = "";
                    for (int i = 0; i < userInput.length(); i++) {
                        if (userInput.charAt(i) == ' ') {
                            command = userInput.substring(1, count);
                            break;
                        } else {
                            count++;
                            if (count == userInput.length() - 3) {
                                command = userInput.substring(1);
                            }
                        }
                    }
                    System.out.println(sdf.format(connectTime) + " [Client] Unknown command " + command);
                } else {
                    ChatMessage chatMessage = new ChatMessage(userInput);

                    oos.writeObject(chatMessage);
                    oos.flush();
                }
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        } catch (IOException e) {
            System.err.println(String.format("Cannot connect to %s on port %s", args[0], args[1]));
            return;
        }
    }
}
