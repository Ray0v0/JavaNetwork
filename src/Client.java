import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    static GUI gui;
    static OutputStream output;
    static BufferedWriter writer;
    static String ip;
    private static String userName = "Ray";
    public static void main(String[] args) throws IOException {
        ip = "10.13.249.60";
        gui = new GUI();
        gui.go();
//        Socket sock = new Socket("121.5.129.39", 8080); // 连接指定服务器和端口
//        Socket sock = new Socket("10.13.207.93", 8080);

    }
    public static void run() throws IOException {
        Socket sock = new Socket(ip, 8080);
        Thread tReceive = new Receive(sock);
        tReceive.start();
        output = sock.getOutputStream();
        writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        writer.write(userName);
        writer.newLine();
        writer.flush();
    }
    public static void send(String s) throws IOException {
        writer.write(s);
        writer.newLine();
        writer.flush();
    }
    public static void setUserName(String userName1) {
        userName = userName1;
    }
    public static void setIP(String ip1) {
        ip = ip1;
    }

    static class Receive extends Thread {
        String userName;
        Socket sock;

        public Receive(Socket sock) {
            this.sock = sock;
        }

        @Override
        public void run() {
            try (InputStream input = this.sock.getInputStream()) {
                try (OutputStream output = this.sock.getOutputStream()) {
                    handle(input, output);
                }
            } catch (Exception e) {
                try {
                    this.sock.close();
                } catch (IOException ignored) {}
                System.out.println(sock.getRemoteSocketAddress()+" disconnected.");
            }
        }

        private void handle(InputStream input, OutputStream output) throws IOException {
            var writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
            var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            userName = reader.readLine();
            while (true) {
                String s = reader.readLine();
                gui.addElement("[%s] %s".formatted(userName, s));
            }
        }
    }
}