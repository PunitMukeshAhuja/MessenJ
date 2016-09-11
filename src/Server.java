import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Samriddha Basu on 9/8/2016.
 */
public class Server extends JFrame {
    private JTextField inputField;
    private JTextArea textArea;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ServerSocket serverSocket;
    private Socket socket;
    private int port;
    private String user;
    private JButton sendButton;
    private JPanel contentPane;
    private JButton shareFileButton;
    private JLabel infoLabel;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    public Server(int port, @Nullable String user) {
        super("MessenJ IM - Server");
        setContentPane(contentPane);
        setSize(640, 480);
        setLocationByPlatform(true);
        getRootPane().setDefaultButton(sendButton);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.port = port;
        if (!user.equals("")) {
            this.user = user;
        } else this.user = "SERVER";
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
        shareFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new JFileChooser().showOpenDialog(contentPane);
            }
        });
        setVisible(true);
        inputField.requestFocus();
        setupServer();
    }

    void setupServer() {
        try {
            serverSocket = new ServerSocket(port, 5);
            while (true) {
                try {
                    waitForConnection();
                    setupStreams();
                    whileConnected();
                } catch (EOFException e) {
                    showMessage("Server ended the connection");
                } finally {
                    close();
                    continue;
                }
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
        try {
            if (text.equals("END")) {
                outputStream.writeObject(text);
                outputStream.flush();
            } else {
                outputStream.writeObject(user + ": " + text);
                outputStream.flush();
                showMessage(user + ": " + text);
            }
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

    void waitForConnection() throws IOException {
        showMessage("Waiting to connect...");
        socket = serverSocket.accept();
        showMessage("Connecting to " + socket.getInetAddress().getHostName());
    }

    void setupStreams() throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        showMessage("Connection established");
        infoLabel.setText("Connected as " + user + " to " + socket.getInetAddress().getHostName() + ":" + port);
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
            } catch (SocketException e) {
                e.printStackTrace();
                break;
            }
        } while (!message.equals("END"));
    }

    void close() {
        showMessage("Closing all connections...");
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            showMessage("All connections closed.");
            allowTyping(false);
            infoLabel.setText("Not connected");
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
