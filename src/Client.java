import com.sun.istack.internal.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Samriddha Basu on 9/8/2016.
 */
public class Client extends JFrame {
    JTextField inputField;
    JTextArea textArea;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    Socket socket;
    String serverIP;
    int serverPort;
    String user;
    String message = "";
    private JButton sendButton;
    private JPanel contentPane;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    public Client(String host, int port, @Nullable String user) {
        super("MessenJ IM - Client");
        setContentPane(contentPane);
        setSize(640, 480);
        setLocationByPlatform(true);
        getRootPane().setDefaultButton(sendButton);

        if (!user.equals("")) {
            this.user = user;
        } else this.user = "Client";
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        serverIP = host;
        serverPort = port;
        allowTyping(false);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                if (!text.equals("")) {
                    send(text);
                    inputField.setText("");
                }
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                if (!text.equals("")) {
                    send(text);
                    inputField.setText("");
                }
            }
        });
        setVisible(true);
        setup();
    }

    void setup() {
        try {
            connect();
            setupStreams();
            whileConnected();
        } catch (EOFException e) {
            showMessage("Client terminated connection");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    void allowTyping(final boolean allowed) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                inputField.setEditable(allowed);
            }
        });
    }

    void send(String text) {
        try {
            outputStream.writeObject(user + ": " + text);
            outputStream.flush();
            showMessage(user + ": " + text);
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Couldn\'t send your message");
        }
    }

    void showMessage(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append(text + "\n");
            }
        });
    }

    void connect() throws IOException {
        try {
            showMessage("Attempting connection to server...");
            socket = new Socket(InetAddress.getByName(serverIP), serverPort);
            showMessage("Connecting to " + socket.getInetAddress().getHostName());
        } catch (ConnectException e) {
            showMessage("Could not connect to server at that address");
        }
    }

    void setupStreams() throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        showMessage("Connection established");
    }

    void whileConnected() throws IOException {
        String message = "";
        allowTyping(true);
        do {
            try {
                message = (String) inputStream.readObject();
                showMessage(message);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                showMessage("Something went wrong, cannot display message");
            }
        } while (!message.equals("SERVER: END"));
    }

    void close() {
        showMessage("Closing all connections...");
        allowTyping(false);
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            showMessage("All connections closed.");
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane1.setViewportView(textArea);
        inputField = new JTextField();
        contentPane.add(inputField, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Send");
        contentPane.add(sendButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
