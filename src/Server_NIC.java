import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Server_NIC {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("server is running...");
        System.out.println("本机IP: " + InetAddress.getLocalHost().getHostAddress());
        while (true) {
            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            System.out.println(socket.getRemoteSocketAddress() + " connected.");
            Thread tConnect = new Connect(socket, reader, writer);
            tConnect.start();
        }
    }
}
class Connect extends Thread {
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    public Connect(Socket socket, BufferedReader reader, BufferedWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }
    public void run() {
        while (true) {
            try {
                String command = reader.readLine();
                String[] commands = command.split(" ");
                if (commands[0].equals("login")) {
                    login(commands[1], commands[2]);
                } else if (commands[0].equals("register")) {
                    register(commands[1], commands[2]);
                } else if (commands[0].equals("friends")) {
                    getFriends(commands[1]);
                } else if (commands[0].equals("receives")) {
                    getReceives(commands[1], commands[2]);
                } else if (commands[0].equals("addFriend")) {
                    addFriend(commands[1], commands[2]);
                } else if (commands[0].equals("message")) {
                    addMessage(commands[1], commands[2]);
                }
            } catch (IOException e) {
                System.out.println(socket.getRemoteSocketAddress() + " disconnected.");
                try {
                    socket.close();
                } catch (IOException ignore) {
                }
                break;
            }
        }
    }
    public void addMessage(String username, String friendName) throws IOException {
        String path = "data/account/message/" + username + "/" + friendName + ".txt";
        String message = reader.readLine();
        addLineInFile(path, message);

        path = "data/account/message/" + friendName + "/" + username + ".txt";
        addLineInFile(path, message);

        writer.write("succeed");
        writer.newLine();
        writer.flush();
    }
    public void addFriend(String friendName, String username) throws IOException {
        if (hasUsername(friendName)) {
            if (!hasFriend(username, friendName)) {
                String path = "data/account/message/" + username + "/friends.txt";
                addLineInFile(path, friendName);

                path = "data/account/message/" + friendName + "/friends.txt";
                addLineInFile(path, username);

                File textFile1 = new File("data/account/message/" + username + "/" + friendName + ".txt");
                textFile1.createNewFile();

                File textFile2 = new File("data/account/message/" + friendName + "/" + username + ".txt");
                textFile2.createNewFile();

                writer.write("Add a friend successfully.");
                writer.newLine();
                writer.flush();
            } else {
//                System.out.println(2);
                writer.write("All ready has this friend.");
                writer.newLine();
                writer.flush();
            }
        } else {
//            System.out.println(3);
            writer.write("No such User.");
            writer.newLine();
            writer.flush();
        }
    }
    private boolean hasFriend(String username, String friendName) throws IOException{
        File fFriends = new File("data/account/message/" + username + "/friends.txt");
        BufferedReader fFriendsReader = new BufferedReader(new FileReader(fFriends));
        String line;
        boolean ret = false;
        while ((line = fFriendsReader.readLine()) != null) {
            if (line.equals(friendName)) {
                ret = true;
                break;
            }
        }
        return ret;
    }
    public void login(String username, String password) throws IOException {
        if (hasUsername(username)) {
            File fUser = new File("data/account/password/" + username + ".txt");
            BufferedReader fUsersReader = new BufferedReader(new FileReader(fUser));
            if (password.equals(fUsersReader.readLine())) {
                writer.write("Log in successfully.");
                writer.newLine();
                writer.flush();
            } else {
                writer.write("Password is wrong.");
                writer.newLine();
                writer.flush();
            }
        } else {
            writer.write("Username not found.");
            writer.newLine();
            writer.flush();
        }
    }
    public void register(String username, String password) throws IOException {
        if (hasUsername(username)) {
            writer.write("Username has been used.");
            writer.newLine();
            writer.flush();
        } else {
            addLineInFile("data/account/usernames.txt", username);
            File fUser = new File("data/account/password/" + username + ".txt");
            fUser.createNewFile();
            BufferedWriter fUserWriter = new BufferedWriter(new FileWriter(fUser));
            fUserWriter.write(password);
            fUserWriter.newLine();
            fUserWriter.flush();

            File dir = new File("data/account/message/" + username);
            dir.mkdir();

            File textFile = new File("data/account/message/" + username + "/friends.txt");
            textFile.createNewFile();

            writer.write("Account has been created successfully");
            writer.newLine();
            writer.flush();
        }
    }
    private void getFriends(String username) throws IOException {
        File fFriends = new File("data/account/message/" + username + "/friends.txt");
        BufferedReader fFriendsReader = new BufferedReader(new FileReader(fFriends));
        String line = fFriendsReader.readLine();
        StringBuilder sb = new StringBuilder();
        if (line != null) {
            sb.append(line);
            while ((line = fFriendsReader.readLine()) != null) {
                sb.append(' ');
                sb.append(line);
            }
        }
        fFriendsReader.close();
        writer.write(sb.toString());
        writer.newLine();
        writer.flush();
    }
    private boolean hasUsername(String username) throws IOException {
        File fUsernames = new File("data/account/usernames.txt");
        BufferedReader fUsernamesReader = new BufferedReader(new FileReader(fUsernames));
        String line;
        boolean hasUsername = false;
        while ((line = fUsernamesReader.readLine()) != null) {
            if (line.equals(username)) {
                hasUsername = true;
                break;
            }
        }
        fUsernamesReader.close();
        return hasUsername;
    }
    private void addLineInFile(String path, String addLine) throws IOException {
        File file = new File(path);
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        String line;
        ArrayList<String> lines = new ArrayList<>();
        while ((line = fileReader.readLine()) != null) {
            lines.add(line);
        }
        fileReader.close();
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
        for (String s : lines) {
            fileWriter.write(s);
            fileWriter.newLine();
        }
        fileWriter.write(addLine);
        fileWriter.newLine();
        fileWriter.flush();
        fileWriter.close();
    }
    private void getReceives(String username, String friendName) throws IOException{
        File fReceives = new File("data/account/message/" + username + "/" + friendName + ".txt");
        BufferedReader fReceivesReader = new BufferedReader(new FileReader(fReceives));
        String line = fReceivesReader.readLine();
        StringBuilder sb = new StringBuilder();
        if (line != null) {
            sb.append(line);
            while ((line = fReceivesReader.readLine()) != null) {
                sb.append("\n");
                sb.append(line);
            }
        }
        sb.append("\n");
        sb.append("end.");
        fReceivesReader.close();
//        System.out.println(sb.toString());
        writer.write(sb.toString());
        writer.newLine();
        writer.flush();
    }
}