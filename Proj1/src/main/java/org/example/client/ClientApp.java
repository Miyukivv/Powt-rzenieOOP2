package org.example.client;

/*

2. W projekcie pierwszym napisz aplikację kliencką, która będzie
wczytywać ze standardowego wejścia nazwę użytkownika oraz ścieżkę do pliku csv.
Następnie wyśle na serwer informację o nazwie użytkownika, oraz zawartość pliku csv (w przykładzie są to tm00.csv i tm01.csv ) linia po linii.

Po każdej wysłanej linii mają nastąpić 2 sekundy przerwy.
Zakładamy, że podawane są unikatowe nazwy użytkowników.
Po zakończeniu aplikacja wyśle wiadomość informującą serwer o zakończeniu przesyłania(np. bye).
Wysyłanie wszystkich tych informacji odseparuj w oddzielnej funkcji: public void sendData(String name, String filepath)
 */

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) throws IOException, InterruptedException {

        //wczytywać ze standardowego wejścia nazwę użytkownika oraz ścieżkę do pliku csv.
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter username:");
        String username = scanner.nextLine();

        System.out.println("Enter path to CSV file:");
        String csvFilePath = scanner.nextLine();

        ClientApp clientApp = new ClientApp();
        clientApp.sendData(username, csvFilePath);
    }

    public void sendData(String name, String filepath) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 1234);
        PrintWriter senderToServer = new PrintWriter(socket.getOutputStream(), true); //Tutaj chcemy wysyłać, czyli będzie getOutputStream, bo wysyłamy do Socketa

    //Następnie wyśle na serwer informację o nazwie użytkownika,
        senderToServer.println(name);

//oraz zawartość pliku csv (w przykładzie są to tm00.csv i tm01.csv ) linia po linii.
        File csvFile = new File(filepath);
        FileReader fr = new FileReader(csvFile);
        BufferedReader br = new BufferedReader(fr);
        String line = "";
        while((line = br.readLine()) != null){
            senderToServer.println(line);
//            Po każdej wysłanej linii mają nastąpić 2 sekundy przerwy.
            Thread.sleep(2000);
        }

//        Po zakończeniu aplikacja wyśle wiadomość informującą serwer o zakończeniu przesyłania(np. bye).
        senderToServer.println("bye");
    }


}
