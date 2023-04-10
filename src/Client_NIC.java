import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static java.lang.Thread.*;

public class Client_NIC {
    static BufferedReader reader;
    static BufferedWriter writer;
    static GateGUI gateGUI;
    static MainGUI mainGUI;
    public static void main(String[] args) throws IOException {
        String ip;
        ip = "121.5.129.39";
//        ip = "10.27.40.151";
        Socket socket = new Socket(ip, 8080);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        gateGUI = new GateGUI();
        gateGUI.run();
    }
    private static void sendMessage(String username, String friendName, String message) throws IOException {
        writer.write("message " + username + " " + friendName);
        writer.newLine();
        writer.flush();
        writer.write("["+ username + "] " + message);
        writer.newLine();
        writer.flush();
        reader.readLine();
        mainGUI.run();
    }
    private static class TUpdate extends Thread {
        @Override
        public void run() {
            System.out.println("a");
            while (true) {
                try {
                    sleep(1000);
                } catch (InterruptedException ignore) { }
                try {
//                    System.out.println("a");
                    mainGUI.updateReceiveArea();
                } catch (IOException ignore) { }
            }
        }

    }
    private static void login(String command) throws IOException{
        writer.write(command);
        writer.newLine();
        writer.flush();
        String commandBack = reader.readLine();
        MessageGUI messageGUI = new MessageGUI(commandBack);
        messageGUI.run();
        if (commandBack.equals("Log in successfully.")) {
            gateGUI.hide();
            mainGUI = new MainGUI(command.split(" ")[1]);
            mainGUI.run();
            TUpdate tUpdate = new TUpdate();
            tUpdate.start();
        }
    }
    private static void register(String command) throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
        String commandBack = reader.readLine();
        MessageGUI messageGUI = new MessageGUI(commandBack);
        messageGUI.run();
    }
    private static String[] getFriends(String username) throws IOException {
        writer.write("friends " + username);
        writer.newLine();
        writer.flush();
        String friendsInString = reader.readLine();
        if (friendsInString == null) {
            return new String[0];
        } else {
            return friendsInString.split(" ");
        }
    }
    private static String getReceives(String username, String friendName) throws IOException {
        writer.write("receives " + username + " " + friendName);
        writer.newLine();
        writer.flush();
        String line = reader.readLine();
        StringBuilder sb = new StringBuilder();
        if (line != null) {
            sb.append(line);
            while (!Objects.equals(line = reader.readLine(), "end.")) {
                sb.append("\n");
                sb.append(line);
            }
        }
//        System.out.println(sb.toString());
        return sb.toString();
    }
    private static void addFriend(String friendName, String username) throws IOException {
        writer.write("addFriend " + friendName + " " + username);
        writer.newLine();
        writer.flush();
        MessageGUI messageGUI = new MessageGUI(reader.readLine());
        messageGUI.run();
        mainGUI.run();
    }
    private static class GateGUI {
        JFrame gateFrame;
        JTextField usernameTextField, passwordTextField;
        public void run() {
            JLabel usernameLabel = new JLabel("username: ");
            JLabel passwordLabel = new JLabel("password: ");

            usernameTextField = new JTextField();
            passwordTextField = new JTextField();

            JButton loginButton = new JButton("登录");
            loginButton.addActionListener(new LoginButtonListener());

            JButton registerButton = new JButton("注册");
            registerButton.addActionListener(new RegisterButtonListener());

            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
            labelPanel.add(usernameLabel);
            labelPanel.add(passwordLabel);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.add(usernameTextField);
            textPanel.add(passwordTextField);

            JPanel upperPanel = new JPanel();
            upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
            upperPanel.add(labelPanel);
            upperPanel.add(textPanel);

            JPanel lowerPanel = new JPanel();
            lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
            lowerPanel.add(loginButton);
            lowerPanel.add(registerButton);

            gateFrame = new JFrame();
            gateFrame.getContentPane().add(BorderLayout.CENTER, upperPanel);
            gateFrame.getContentPane().add(BorderLayout.SOUTH, lowerPanel);
            gateFrame.setSize(300, 100);
            gateFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gateFrame.setVisible(true);
        }
        public void hide() {
            gateFrame.setVisible(false);
        }
        private class LoginButtonListener implements ActionListener{

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Objects.equals(usernameTextField.getText(), "") && !Objects.equals(passwordTextField.getText(), "")) {
                    try {
                        Client_NIC.login("login %s %s".formatted(usernameTextField.getText(), passwordTextField.getText()));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    MessageGUI messageGUI = new MessageGUI("Information not complete.");
                    messageGUI.run();
                }
            }
        }
        private class RegisterButtonListener implements ActionListener{

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Objects.equals(usernameTextField.getText(), "") && !Objects.equals(passwordTextField.getText(), "")) {
                    try {
                        Client_NIC.register("register %s %s".formatted(usernameTextField.getText(), passwordTextField.getText()));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    MessageGUI messageGUI = new MessageGUI("Information not complete.");
                    messageGUI.run();
                }
            }
        }
    }
    private static class MessageGUI {
        String message;
        JFrame messageFrame;
        public MessageGUI(String message) {
            this.message = message;
        }
        public void run() {
            JLabel messageLabel = new JLabel(" " + message);

            messageFrame = new JFrame("提示");

            messageFrame.getContentPane().add(BorderLayout.CENTER, messageLabel);
            messageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            messageFrame.setSize(200, 100);
            messageFrame.setVisible(true);
        }
    }
    private static class MainGUI {
        JFrame mainFrame = new JFrame();
        JTextArea receiveArea;
        JTextArea sendArea;
        JButton[] friendButtons;
        String[] friends;
        String username;
        int friendNum;
        int friendPointer = 0;
        public MainGUI(String username) {
            this.username = username;
        }
        public void run() throws IOException {
            friends = Client_NIC.getFriends(username);
            friendNum = friends.length;
            if (friends[0].equals("") && friendNum == 1) {
                friendNum = 0;
                friends = new String[0];
            }

            friendButtons = new JButton[friendNum];
            JPanel friendsPanel = new JPanel();
            friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
            for (int i = 0; i < friendNum; i++) {
                friendButtons[i] = new JButton("%s".formatted(friends[i]));
                friendButtons[i].setContentAreaFilled(false);
                friendButtons[i].setBorderPainted(false);
                friendButtons[i].addActionListener(new FriendButtonsListener());
                friendsPanel.add(friendButtons[i]);
            }

            JButton addFriendButton = new JButton("+");
            addFriendButton.addActionListener(new AddFriendButtonListener());
            friendsPanel.add(addFriendButton);

            receiveArea = new JTextArea(20, 20);
            if (friendNum != 0) {
                updateReceiveArea();
            }

            sendArea = new JTextArea(15, 20);
            sendArea.setBackground(Color.lightGray);
            sendArea.setText("");

            JButton sendButton = new JButton("发送");
            sendButton.addActionListener(new SendButtonListener());

            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.add(receiveArea);
            messagePanel.add(sendArea);
            messagePanel.add(sendButton);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
            mainPanel.add(friendsPanel);
            mainPanel.add(messagePanel);

            mainFrame.setContentPane(mainPanel);
            mainFrame.setSize(700, 500);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setVisible(true);
        }
        private void updateReceiveArea() throws IOException {
            String receives = Client_NIC.getReceives(username, friends[friendPointer]);
            receiveArea.setText(receives);
            mainFrame.repaint();
        }
        private class FriendButtonsListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < friendNum; i++) {
                    if (e.getSource() == friendButtons[i]) {
                        friendPointer = i;
                    }
                }
                try {
                    updateReceiveArea();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        private class AddFriendButtonListener implements ActionListener{

            @Override
            public void actionPerformed(ActionEvent e) {
                ReadMessageGUI readMessageGUI = new ReadMessageGUI("添加好友", username);
                readMessageGUI.run();
            }
        }
        private class SendButtonListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                String message = sendArea.getText();
                sendArea.setText("");
                try {
                    Client_NIC.sendMessage(username, friends[friendPointer], message);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    private static class ReadMessageGUI {
        String title;
        String username;
        JFrame readMessageGUI;
        JTextField friendNameText;
        public ReadMessageGUI(String title, String username) {
            this.title = title;
            this.username = username;
        }
        public void run() {
            friendNameText = new JTextField();
            friendNameText.setText("");

            JButton confirmButton = new JButton("确认");
            confirmButton.addActionListener(new ConfirmButtonListener());

            readMessageGUI = new JFrame(title);
            readMessageGUI.getContentPane().add(BorderLayout.CENTER, friendNameText);
            readMessageGUI.getContentPane().add(BorderLayout.SOUTH, confirmButton);
            readMessageGUI.setSize(200, 100);
            readMessageGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            readMessageGUI.setVisible(true);
        }
        private class ConfirmButtonListener implements ActionListener{

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Client_NIC.addFriend(friendNameText.getText(), username);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                readMessageGUI.setVisible(false);
            }
        }
    }
}

