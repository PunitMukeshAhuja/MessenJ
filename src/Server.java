import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.URL;

/**
 * Created by Samriddha Basu on 9/8/2016.
 */
public class Server extends JFrame {
    JLabel infoLabel;
    private JTextField inputField;
    private JTextArea textArea;
    private ServerSocket serverSocket;
    private int connections;
    private int port;
    private String user;
    private int number;
    private JButton sendButton;
    private JPanel contentPane;
    private JButton shareFileButton;
    private ClientThread[] clientThreads;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    public Server(int port, @Nullable final String user, int number) {
        super("MessenJ IM - Server");
        setContentPane(contentPane);
        setSize(640, 480);
        setLocationByPlatform(true);
        getRootPane().setDefaultButton(sendButton);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        connections = 0;
        this.port = port;
        this.user = user;
        this.number = number;
        allowTyping(false);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                if (!text.equals("")) {
                    send(user + ": " + text);
                    inputField.setText("");
                }
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                if (!text.equals("")) {
                    send(user + ": " + text);
                    inputField.setText("");
                }
            }
        });
        shareFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new JFileChooser().showOpenDialog(contentPane);
            }
        });
        setVisible(true);
        inputField.requestFocus();
        clientThreads = new ClientThread[number];
        for (int i = 0; i < number; i++) clientThreads[i] = null;
        setupServer();
    }

    void setupServer() {
        try {
            serverSocket = new ServerSocket(port);
            infoLabel.setText("Username: " + user + " | External IP: " + getExtIp() + " | Port: " + port);
            showMessage("Waiting to connect...");
            while (connections < number) {
                waitForConnection(connections);
            }
        } catch (BindException e) {
            showMessage("This port is already in use. Try hosting on a different port");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void allowTyping(final boolean allowed) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                inputField.setEditable(allowed);
                sendButton.setEnabled(allowed);
                shareFileButton.setEnabled(allowed);
            }
        });
    }

    void send(String text) {
        for (int i = 0; i < number; i++) {
            try {
                clientThreads[i].outputStream.writeObject(text);
                clientThreads[i].outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Couldn\'t send your message to user #" + i);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
        showMessage(text);
    }

    void showMessage(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append(text + "\n");
            }
        });
    }

    void waitForConnection(int n) {
        if (clientThreads[n] == null) {
            try {
                clientThreads[n] = new ClientThread(this, serverSocket.accept(), n);
                send("New client connected");
                new Thread(clientThreads[n]).start();
                connections++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void disconnected(int number) {
        clientThreads[number] = null;
        connections--;
        waitForConnection(number);
    }

    String getExtIp() {
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
                String ip = in.readLine();
                return ip;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            return "-";
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
        contentPane.setLayout(new GridLayoutManager(3, 3, new Insets(8, 8, 8, 8), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane1.setViewportView(textArea);
        inputField = new JTextField();
        contentPane.add(inputField, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Send");
        contentPane.add(sendButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("Not connected");
        contentPane.add(infoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shareFileButton = new JButton();
        shareFileButton.setText("Share file");
        contentPane.add(shareFileButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
