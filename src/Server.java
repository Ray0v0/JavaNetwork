import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Server {
    static GUI_Server gui;
    static OutputStream output;
    static BufferedWriter writer;
    static String userName = "RayOvO";
    public static void main(String[] args) throws IOException {
        gui = new GUI_Server();
        gui.go();
        ServerSocket ss = new ServerSocket(8080); // 监听指定端口
        System.out.println("server is running...");
        System.out.println("本机IP: " + InetAddress.getLocalHost().getHostAddress());
        while (true) {
            Socket sock = ss.accept();
            System.out.println("connected from " + sock.getRemoteSocketAddress());
            Thread tReceive = new Receive(sock);
            tReceive.start();
            output = sock.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
            writer.write(userName);
            writer.newLine();
            writer.flush();
        }
    }
    public static void send(String s) throws IOException {
        writer.write(s);
        writer.newLine();
        writer.flush();
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