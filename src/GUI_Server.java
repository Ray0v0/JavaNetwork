import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class GUI_Server implements ActionListener {
    JFrame mainFrame;
    JPanel panel;
    JButton button;
    JTextArea monitorArea;
    JScrollPane scroller;
    JTextArea textArea;
    ArrayList<String> listEntries = new ArrayList<>();

    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.go();
    }
    public void go() {
        mainFrame = new JFrame("服务端聊天窗");
        panel = new JPanel();
        mainFrame.setContentPane(panel);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(600, 400);
        monitorArea = new JTextArea(15, 20);
//        textArea.setEnabled(false);
        monitorArea.setFont(new java.awt.Font("宋体", Font.BOLD, 18));
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
        String s = textArea.getText();
        addElement(s);
        try {
            Server.send(s);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        textArea.setText("");
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
