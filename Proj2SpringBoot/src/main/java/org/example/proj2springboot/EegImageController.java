package org.example.proj2springboot;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RestController
public class EegImageController {
//    TWOJA ŚCIEŻKA
    private static String DATABASE_URL = "jdbc:sqlite:/media/micha/Ubuntu_Documents/egzamin_natka/zadanie2/usereeg.db";

//    http://localhost:8080/record?username=jan&electrodeNumber=18
    @GetMapping("/record")
    public String getRecord(@RequestParam String username, @RequestParam int electrodeNumber) {
        String output = "";

        String selectSQL = "SELECT * FROM user_eeg WHERE username = ? AND electrode_number = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, electrodeNumber);
            var result = pstmt.executeQuery();
            output += "Username: " + result.getString("username") + "\n";
            output += "Electrode number: " + result.getInt("electrode_number") + "\n";
            String base64Image = result.getString("image");
            output += "\n" + "<div>\n" +
                    "  <img src=\"data:image/jpeg;base64," + base64Image + "\" />\n" +
                    "</div>";;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return output;
    }
}
