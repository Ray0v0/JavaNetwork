import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class GUI implements ActionListener {
    JFrame queryFrame;
    JFrame mainFrame;
    JTextField queryUsername;
    JTextField queryIP;
    JPanel labelPanel;
    JPanel textPanel;
    JPanel queryPanel;
    JLabel ipLabel;
    JLabel userNameLabel;
    JPanel panel;
    JButton button;
    JButton queryButton;
    JTextArea monitorArea;
    JScrollPane scroller;
    JTextArea textArea;
    JPanel framePanel = new JPanel();
    ArrayList<String> listEntries = new ArrayList<>();

    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.go();
    }
    public void go() {
        userNameLabel = new JLabel("username: ");
        ipLabel = new JLabel("IP: ");

        queryUsername = new JTextField();
        queryIP = new JTextField();

        queryButton = new JButton("确认");
        queryButton.addActionListener(this);

        labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(userNameLabel);
        labelPanel.add(ipLabel);

        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(queryUsername);
        textPanel.add(queryIP);

        queryPanel = new JPanel();
        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.X_AXIS));
        queryPanel.add(labelPanel);
        queryPanel.add(textPanel);

        framePanel.setLayout(new BoxLayout(framePanel, BoxLayout.Y_AXIS));
        framePanel.add(queryPanel);
        framePanel.add(queryButton);


        queryFrame = new JFrame("连接设置");
        queryFrame.setContentPane(framePanel);
        queryFrame.setSize(300, 100);
        queryFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        queryFrame.setVisible(true);

    }

    public void run() {
        mainFrame = new JFrame("聊天窗");
        panel = new JPanel();
        mainFrame.setContentPane(panel);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(600, 400);
        monitorArea = new JTextArea(15, 20);
//        textArea.setEnabled(false);
        monitorArea.setFont(new java.awt.Font("宋体",Font.BOLD, 18));
        textArea = new JTextArea(5, 20);
        textArea.setFont(new java.awt.Font("宋体", Font.PLAIN, 16));
        scroller = new JScrollPane(monitorArea);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        button = new JButton("发送");
        button.addActionListener(this);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(scroller);
        panel.add(textArea);
        panel.add(button);
        mainFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            String s = textArea.getText();
            addElement(s);
            try {
                Client.send(s);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            textArea.setText("");
        } else {
            Client.setUserName(queryUsername.getText());
            Client.setIP(queryIP.getText());
            queryFrame.setVisible(false);
            try {
                Client.run();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            run();
        }
    }
    public void addElement(String s) {
        listEntries.add(s);
        monitorArea.setText("");
        for (String listEntry : listEntries) {
            monitorArea.append(listEntry);
            monitorArea.append("\n");
        }
    }
}
