package server;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;

import messages.*;

public class GUIClient extends JFrame implements Serializable {
    private JLabel nickname;
    private JButton bChange;
    private JButton bSend;
    private JTextArea chatBox;
    private JTextArea textBox;

    public GUIClient() {
        super("Chat client");

        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        createUsername();
        createDisplayedBox();
        createInputBox();

        /**
         * Redirects System.out to chatBox
         */
        PrintStream printStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // redirects data to the text area
                chatBox.append(String.valueOf((char) b));

                // scrolls the text area to the end of data
                chatBox.setCaretPosition(chatBox.getDocument().getLength());
            }
        });
        System.setOut(printStream);

//        InputStream input = new BufferedInputStream() {
//            @Override
//            public int read() throws IOException {
//
//                return 0;
//            }
//        };
//
//        System.setIn(input);

        this.getRootPane().setDefaultButton(bSend);
        this.setVisible(true);
    }

    private void addBorder(JComponent component, String title) {
        Border etch = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        Border tb = BorderFactory.createTitledBorder(etch, title);
        component.setBorder(tb);
    }

    private void createUsername() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        addBorder(p, "username");

        nickname = new JLabel("  " + "Nickname");
        p.add(nickname);

        p.add(Box.createHorizontalGlue());

        bChange = new JButton("Change nick");
        bChange.addActionListener(e -> changeNick());
        p.add(bChange);

        GridBagConstraints pc = new GridBagConstraints();
        pc.anchor = GridBagConstraints.PAGE_START;
        pc.fill = GridBagConstraints.HORIZONTAL;
        pc.weightx = 1;
        this.add(p, pc);
    }

    public void createDisplayedBox() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        addBorder(p, "screen");

        chatBox = new JTextArea();
        chatBox.setEditable(false);
        chatBox.setFont(new Font("Ubuntu Mono", Font.PLAIN, 15));
        chatBox.setLineWrap(true);
        chatBox.setWrapStyleWord(true);

        p.add(new JScrollPane(chatBox));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 6;

        this.add(p, c);
    }

    private void createInputBox() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        addBorder(p, "chat area");

        textBox = new JTextArea();
        textBox.setLineWrap(true);
        textBox.setWrapStyleWord(true);
//        textBox.addKeyListener(new KeyAdapter() {
//            public void keyPressed(KeyEvent e){
//                //save the last lines for console to variable input
//                if(e.getKeyCode() == KeyEvent.VK_ENTER){
//                    try {
//                        int line = textBox.getLineCount() -2;
//                        int start = textBox.getLineStartOffset(line);
//                        int end = textBox.getLineEndOffset(line);
//                        String input = textBox.getText(start, end  - start);
//                    } catch (Exception e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//        });
        p.add(new JScrollPane(textBox));

        bSend = new JButton("Send");
        bSend.setSize(75, 75);
        bSend.addActionListener(e -> send());
        p.add(bSend);

        GridBagConstraints pc = new GridBagConstraints();
        pc.gridy = 3;
        pc.fill = GridBagConstraints.BOTH;
        pc.weighty = 2;
        this.add(p, pc);
    }

    private void changeNick() {
        String newNick = textBox.getText();
        if (!newNick.equals("")) {
            nickname.setText("  " + newNick);
            textBox.setText(null);


        }
    }

    private void keyReleased() {

    }

    private void send() {
        String userInput = textBox.getText();
        System.out.println(userInput);
    }
}
