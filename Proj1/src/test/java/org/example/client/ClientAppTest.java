package org.example.client;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

public class ClientAppTest{

/*
4.Napisz sparametryzowany test klienckiej metody sendData, który po wysłaniu danych sprawdzi czy otrzymaliśmy oczekiwany obrazek.
Przykład parametrów w pliku test.csv. Wykresy użyte do testu są typu png o rozmiarze 200x100, tło ma kolor biały,
punkty mają kolor czerwony i są rysowane od połowy wysokości (odjęcie wartości pomiaru od połowy wysokości)

Do ich narysowania użyto BufferedImage image oraz Graphics2d. Punkty są prostokątami o wymiarach 1x1.
Przykładowe pliki wczytane w teście to (test02.csv i test01.csv).
 */
@ParameterizedTest
@CsvFileSource(resources = "/test.csv", numLinesToSkip = 1)
void testSendData(String username, String filepath, int electrodeNumber, String expectedImageBase64) throws Exception {

    //Tworzymy CountDownLatch z wartością początkową 1,
    //ponieważ tylko jeden wątek (serwer) musi sygnalizować, że jest gotowy.
    CountDownLatch latch = new CountDownLatch(1); //Pozwala jednemu lub większej ilości wątków czekać, aż inne wątki ukończą wykonywanie pewnych operacji

    //Uruchomienie servera w nowym wątku
    ServerThread serverThread = new ServerThread(latch);
    Thread thread = new Thread(serverThread);
    thread.start();

    //Kiedy serwer jest gotowy do odbioru danych, wywołuje 'countDown()' , co zmniejsza licznik do zera i odblokowuje oczekujące wątki

//Klient wywołuje await(), aby czekać, aż serwer będzie gotowy.
// To blokuje wykonanie klienta, dopóki licznik CountDownLatch nie osiągnie zera.
    latch.await();

    ClientApp clientApp = new ClientApp();

    //Po otrzymaniu sygnału od serwera (licznik osiągnął zero), klient wysyła dane do serwera.
    clientApp.sendData(username, filepath);

    thread.join(); // Wait for server to finish processing

    String receivedImageBase64 = serverThread.getReceivedImageBase64();
    assertEquals(expectedImageBase64, receivedImageBase64);
}

    static class ServerThread implements Runnable {
        private final CountDownLatch latch;
        private String receivedImageBase64;

        ServerThread(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(1234)) {
                latch.countDown(); // Signal that the server is ready

                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String username = in.readLine(); // Read username
                    StringBuilder receivedData = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        if ("bye".equalsIgnoreCase(line)) {
                            break;
                        }
                        receivedImageBase64 = convertDataLineToBase64Graph(Arrays.stream(line.split(",")).map(Float::parseFloat).toList());
                        receivedData.append(line).append("\n");
                    }

                    // Simulate processing and generating base64 image

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String convertDataLineToBase64Graph(List<Float> dataLine) throws IOException {
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

        String getReceivedImageBase64() {
            return receivedImageBase64;
        }
    }
}