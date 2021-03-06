import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;

/**
 * Created by Samriddha Basu on 9/8/2016.
 */
public class Server extends JFrame {
    JLabel infoLabel;
    String[] users;
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
    private JTextArea userText;
    private JFileChooser fileChooser;
    private ClientHandler[] clientHandlers;

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
        fileChooser = new JFileChooser();

        connections = 0;
        this.port = port;
        this.user = user;
        this.number = number;
        users = new String[number + 1];
        users[0] = user;
        allowTyping(false);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                if (!text.equals("")) {
                    send(new Message(Message.TYPE_TEXT, text, user));
                    inputField.setText("");
                }
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                if (!text.equals("")) {
                    send(new Message(Message.TYPE_TEXT, text, user));
                    inputField.setText("");
                }
            }
        });
        shareFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
                    Message message;
                    try {
                        File file = fileChooser.getSelectedFile();
                        message = new Message(Message.TYPE_FILE, Files.readAllBytes(file.toPath()), file.getName(), user);
                        send(message);
                    } catch (IOException e1) {
                        showMessage(new Message("Error sending file"));
                        e1.printStackTrace();
                    }
                }
            }
        });
        setVisible(true);
        inputField.requestFocus();
        clientHandlers = new ClientHandler[number];
        for (int i = 0; i < number; i++) clientHandlers[i] = null;
        setupServer();
    }

    void setupServer() {
        try {
            serverSocket = new ServerSocket(port);
            infoLabel.setText("Username: " + user + " | External IP: " + getExtIp() + " | Port: " + port);
            showMessage(new Message("Waiting to connect..."));
            while (connections < number) {
                waitForConnection(connections);
            }
        } catch (BindException e) {
            showMessage(new Message("This port is already in use. Try hosting on a different port"));
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

    void send(Message message) {
        allowTyping(false);
        for (int i = 0; i < number; i++) {
            try {
                clientHandlers[i].outputStream.writeObject(message);
                clientHandlers[i].outputStream.flush();
            } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            } catch (IOException e) {
                e.printStackTrace();
                showMessage(new Message("Couldn\'t send your message to user #" + i));
            }
        }
        showMessage(message);
        allowTyping(true);
    }

    void showMessage(final Message message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                switch (message.getType()) {
                    case Message.TYPE_CONNECT:
                        textArea.append(message.getText() + "\n");
                        updateUserList();
                        break;
                    case Message.TYPE_DISCONNECT:
                        users[message.getNumber() + 1] = null;
                        updateUserList();
                    case Message.TYPE_ANNOUNCE:
                        textArea.append(message.getText() + "\n");
                        break;
                    case Message.TYPE_TEXT:
                        textArea.append(message.getSender() + ": " + message.getText() + "\n");
                        break;
                    case Message.TYPE_FILE:
                        try {
                            new File(System.getProperty("user.home")
                                    + File.separator
                                    + "MessenJ")
                                    .mkdirs();
                            File newFile = new File(System.getProperty("user.home")
                                    + File.separator
                                    + "MessenJ"
                                    + File.separator
                                    + message.getText());
                            showMessage(new Message(message.getSender() + " is sending file: " + message.getText()));
                            FileOutputStream writer = new FileOutputStream(newFile);
                            writer.write(message.getData());
                            writer.close();
                            showMessage(new Message("File transfer complete"));
                            showMessage(new Message("Saved to: " + newFile.toPath()));
                        } catch (IOException e) {
                            showMessage(new Message("Error transferring file"));
                        }
                        break;
                }
            }
        });
    }

    private void updateUserList() {
        userText.setText("");
        for (int i = 0; i < users.length; i++)
            if (users[i] != null) userText.append(users[i] + "\n");
        userText.updateUI();
    }

    void waitForConnection(int n) {
        if (clientHandlers[n] == null) {
            try {
                clientHandlers[n] = new ClientHandler(this, serverSocket.accept(), n);
                showMessage(new Message("Incoming connection..."));
                new Thread(clientHandlers[n]).start();
                connections++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void disconnected(int number) {
        clientHandlers[number] = null;
        connections--;
        send(new Message(Message.TYPE_DISCONNECT, number, users[number + 1] + " disconnected", users[number + 1]));
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
        contentPane.setLayout(new GridLayoutManager(4, 3, new Insets(8, 8, 8, 8), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane1.setViewportView(textArea);
        inputField = new JTextField();
        contentPane.add(inputField, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        infoLabel = new JLabel();
        infoLabel.setText("Not connected");
        contentPane.add(infoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        contentPane.add(scrollPane2, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        userText = new JTextArea();
        userText.setEditable(false);
        scrollPane2.setViewportView(userText);
        sendButton = new JButton();
        sendButton.setText("Send");
        contentPane.add(sendButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shareFileButton = new JButton();
        shareFileButton.setText("Share file");
        contentPane.add(shareFileButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Connected users:");
        contentPane.add(label1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
