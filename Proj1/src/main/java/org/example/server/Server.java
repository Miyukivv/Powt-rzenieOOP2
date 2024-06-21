package org.example.server;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;


/*
3. W projekcie pierwszym napisz aplikacje serwerową, która obsługuje wielu klientów.
Dla pojedynczego klienta serwer pobiera informację o nazwie usera, dla każdej otrzymanej linii tworzy wykres i zapisuje go w formacie base64,
oraz dodaje wiersz do bazy sqlite z nazwą użytkownika, numerem elektrody/linii i wykresem w base64.
Pamiętaj o utworzeniu bazy danych za pomocą klasy Creator.
 */

public class Server {
//    ŚCIEŻKA BĘDZIE TWOJA
    private static final String DATABASE_URL = "jdbc:sqlite:/media/micha/Ubuntu_Documents/egzamin_natka/zadanie2/usereeg.db";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server started on port 1234");


        //Klient sie łączy i wychodzi:
        while (true) {
            Socket clientSocket = serverSocket.accept(); //Nasluchuje, czeka na polaczenie
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clientHandler.start();
        }

    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            //Server z socketa odczytuje sobie rzeczy
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                username = in.readLine(); // First line is the username
                String line;
                int electrodeNumber = 0;

                while ((line = in.readLine()) != null) {
                    if ("bye".equalsIgnoreCase(line)) {
                        break;
                    }

                    String base64Graph = convertDataLineToBase64Graph(Arrays.stream(line.split(",")).map(Float::parseFloat).toList());

                    saveToDatabase(username, electrodeNumber++, base64Graph);
                }

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        public String convertDataLineToBase64Graph(List<Float> dataLine) throws IOException {
            BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            for(int i = 0; i < dataLine.size(); i++) {
                int y0 = image.getHeight() / 2;
                int y = (int) (-dataLine.get(i) + y0);
                image.setRGB(i, y, 0xffff0000);
            }
            ImageIO.write(image, "png", os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        }

        private void saveToDatabase(String username, int electrodeNumber, String image) throws SQLException {
            String insertSQL = "INSERT INTO user_eeg(username, electrode_number, image) VALUES(?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(DATABASE_URL);
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, username);
                pstmt.setInt(2, electrodeNumber);
                pstmt.setString(3, image);
                pstmt.executeUpdate();
            }
        }
    }
}
