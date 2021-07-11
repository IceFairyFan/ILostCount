package com.application.posapplication.controller;

import com.application.posapplication.model.UserResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;

@Controller
public class RegisterController {
    @GetMapping("/Register.html")
    public String registerController(){
        return "Register.html";
    }

    @RequestMapping(value = "/register/post", method = RequestMethod.POST)
    public @ResponseBody UserResponse registerSubmit(HttpServletRequest req, @RequestParam(value="email")String email, @RequestParam(value="password")String password, @RequestParam(value="name")String name){

        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlserver://databasecapstone.database.windows.net:1433;database=DatabaseCapstone;user=capstone@databasecapstone;password=P@ssw0rd;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
            String tableName = "aUserTable";
            Statement stmt = conn.createStatement();

            String sqlQuery = "INSERT INTO " +tableName+ " VALUES ('" +email+ "', '"
                    +password+ "', '"+name+"', 'AuthProvider')";

            stmt.execute(sqlQuery);

            UserResponse userResponse = new UserResponse();
            userResponse.setStatus(true);

            return userResponse;

        }catch(SQLException e){
            e.printStackTrace();
            UserResponse userResponse = new UserResponse();
            userResponse.setStatus(false);
            userResponse.setMessage("Check your information again.");

            return userResponse;
        }
    }
}
