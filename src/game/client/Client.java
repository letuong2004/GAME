package game.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8888;
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {
            Thread readerThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException ignored) {
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            System.out.println("Kết nối tới server thành công. Dùng /help để xem lệnh.");
            while (true) {
                String line = scanner.nextLine();
                if (line == null) {
                    break;
                }
                out.println(line);
                if (line.equalsIgnoreCase("/exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Không thể kết nối tới server: " + e.getMessage());
        }
    }
}
