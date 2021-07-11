package com.application.posapplication.controller;

import com.application.posapplication.model.MeetingModel;
import com.application.posapplication.model.UserResponse;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.catalina.User;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@Controller
public class ProjectDetailFourController {
    @GetMapping("/ProjectDetail4.html")
    public String projectdetailfour(HttpServletRequest req) throws InterruptedException, ExecutionException, SQLException{
        HttpSession session = req.getSession(false);
        String username = (String) session.getAttribute("username");

        if(session != null){
            return "ProjectDetail4.html";
        }else{
            return "";
        }
    }

    @Autowired
    private StanfordCoreNLP stanfordCoreNLP;

//    private Client client;

    @RequestMapping(value="/getallmeetingfour", method= RequestMethod.POST)
    public @ResponseBody
    UserResponse getAllMeetingFour(@RequestParam(value="id")int ids) {

        ArrayList<MeetingModel> listMeeting = new ArrayList<>();

        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlserver://databasecapstone.database.windows.net:1433;database=DatabaseCapstone;user=capstone@databasecapstone;password=P@ssw0rd;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
            String tableName = "cMeetingTable";
            String columnName = "ProjectID";
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE " +columnName+ " = " + ids);
            ResultSet results = statement.executeQuery();

            while (results.next()) {

                int id = results.getInt(1);
                String meetingName = results.getString("MeetingName");
                String meetingDate = results.getString("MeetingDate");
                String inputText = results.getString("InputFile");
                MeetingModel meeting = new MeetingModel(id, meetingName, meetingDate, inputText);

                listMeeting.add(meeting);
            }

            UserResponse userResponse = new UserResponse();
            userResponse.setStatus(true);
            userResponse.setMessage("Data is found");
            userResponse.setData(listMeeting);
            return userResponse;

        }catch (SQLException e){
            e.printStackTrace();
            UserResponse userResponse = new UserResponse();
            userResponse.setStatus(false);
            userResponse.setMessage("Data not found");
            return userResponse;
        }
    }

    @RequestMapping(value="/uploadfour/post", method = RequestMethod.POST)
    public @ResponseBody void uploadpageinputfour(@RequestPart(value="file") MultipartFile multipartFile, @RequestParam(value="title")String title, @RequestParam(value="date")String date, @RequestParam(value="id")String idss, @RequestParam(value="noun") String pdm)
        throws InterruptedException, ExecutionException, SQLException, IOException{
        String meetingId = "";
//        Link file https://drive.google.com/file/d/1DtvIRg4wK8LuXbQmMSDs2WUgTXXp5ufO/view?usp=sharing
//        Link file http://ipsayoeto.com/Narpati/Req2.wav
//        Link file https://onedrive.live.com/download?resid=E3765DADF2A40478%21106

//        String outputFileName = "./audiofiles";
//        MultipartFile fileItem = downloadFileFromURL(url, outputFileName);

        Connection conn = null;
        try{
            String dbURL = "jdbc:sqlserver://databasecapstone.database.windows.net:1433;database=DatabaseCapstone;user=capstone@databasecapstone;password=P@ssw0rd;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
            conn = DriverManager.getConnection(dbURL);

            String folderName = "";
            MultipartFile fileFromPC = uploadFileFromPC(multipartFile, folderName);

            if(conn != null){
                meetingId = uploadInsertFunctionFour(fileFromPC, title, date, idss);
            }
            speechToText(fileFromPC, title, date, meetingId, pdm, folderName);

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private String uploadInsertFunctionFour(MultipartFile multipartFile, String title, String date, String id) throws SQLException {

        String meetingId = "";

        String tableNameA = "cMeetingTable";
        Connection conn = DriverManager.getConnection("jdbc:sqlserver://databasecapstone.database.windows.net:1433;database=DatabaseCapstone;user=capstone@databasecapstone;password=P@ssw0rd;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
//        Statement stmt = conn.createStatement();

//        String insertValueA = "INSERT INTO " + tableNameA + " VALUES ('"+id+"', '" + title + "', '" + date + "', '" + multipartFile.getOriginalFilename() + "')";
        String insertValueA = "INSERT INTO " + tableNameA + " VALUES ('"+id+"', '" + title + "', '" + date + "', '" + multipartFile.getOriginalFilename() + "')";

        PreparedStatement ps = conn.prepareStatement(insertValueA, Statement.RETURN_GENERATED_KEYS);
        ps.executeUpdate();

//        int affectedRows = stmt.executeUpdate(insertValueA);
//        if (affectedRows == 0) {
//            throw new SQLException("Creating user failed, no rows affected.");
//        }

        ResultSet generatedKeys = ps.getGeneratedKeys();
        try{
            if (generatedKeys.next()) {
//                user.setId(generatedKeys.getLong(1));
                meetingId = String.valueOf(generatedKeys.getLong(1));
            }
            else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
        }catch(SQLException x){
            x.printStackTrace();
        }

//        String columnNameMeetingName = "MeetingName";
//        String retrieveMeetingId = "SELECT * FROM " + tableNameA + " WHERE " + columnNameMeetingName + " = '" +title+ "';";
//
//        int meetingIdInt = 0;
//
//        ResultSet result = stmt.executeQuery(retrieveMeetingId);
//        while(result.next()){
//            meetingIdInt = result.getInt("MeetingID");
//        }
//        meetingId = String.valueOf(meetingIdInt);
        return meetingId;
    }

    @PostMapping
    @RequestMapping(value="/uploadfour/post/posnoun")
    public Set<String> uploadPosNounFour(@RequestBody final String input) {

        HashSet<String> tmp = new HashSet();
        return tmp;
    }

    public void processInput(AtomicReference<String> text, String meetingId, String pdm) throws SQLException {

        String texta = text.toString();
        Connection conn = DriverManager.getConnection("jdbc:sqlserver://databasecapstone.database.windows.net:1433;database=DatabaseCapstone;user=capstone@databasecapstone;password=P@ssw0rd;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
        Statement stmt = conn.createStatement();

        String tableNameA = "dMeetingDetailsModalTable";

        CoreDocument coreDocument = new CoreDocument(texta);
        stanfordCoreNLP.annotate(coreDocument);
        List<CoreSentence> coreSentences = coreDocument.sentences();

        for (CoreSentence a : coreSentences) {
            int inta = 0;
            ArrayList<String> aldma = new ArrayList<>();
            ArrayList<String> aldmb = new ArrayList<>();

            String as = a.toString();
            String[] searchWant = as.split("want |need");
            for (String b : searchWant) {
                inta++;
            }
            if(inta>1){
                int x = 0;
                String[] searchModal = as.split("can |could |may |might |should |shall |must | will");
                for(String d : searchModal){
                    if(x<1){
                        aldma.add(d);
                        x++;
                    }else{
                        aldmb.add(d);
                    }
                }
                String output = "As a " + pdm + ", " + aldma + ", so that I can " + aldmb;

                //issues in the variable meetingId
                String insertIntoSQL = "INSERT INTO " + tableNameA + " VALUES ('" +meetingId+ "', '" + pdm + "', '" + aldma + "', '" + aldmb + "', '" + output + "', 'User Story', 'High')";
                stmt.execute(insertIntoSQL);
            }
        }
    }


    @PostMapping("/fromfile")
    public static String fromFile(SpeechConfig speechConfig, MultipartFile file) throws InterruptedException,
            ExecutionException {
        AudioConfig audioConfig = AudioConfig.fromWavFileInput(file.getOriginalFilename());
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);

//        Future<SpeechRecognitionResult> task = recognizer.recognizeOnceAsync();
//        Future task = recognizer.startContinuousRecognitionAsync();
        Future task = recognizer.startContinuousRecognitionAsync();

        Object result = task.get();
        String outputText = result.toString();

        System.out.println(outputText);
        return outputText;

//        SpeechRecognitionResult result = task.get();
//
//        switch (result.getReason()) {
//            case RecognizedSpeech:
//                System.out.println("We recognized: " + result.getText());
//                break;
//            case NoMatch:
//                System.out.println("NOMATCH: Speech could not be recognized.");
//                break;
//            case Canceled: {
//                CancellationDetails cancellation = CancellationDetails.fromResult(result);
//                System.out.println("CANCELED: Reason = " + cancellation.getReason());
//
//                if (cancellation.getReason() == CancellationReason.Error) {
//                    System.out.println("CANCELED: ErrorCode = " + cancellation.getErrorCode());
//                    System.out.println("CANCELED: ErrorDetails = " + cancellation.getErrorDetails());
//                    System.out.println("CANCELED: Did you update the subscription info?");
//                }
//            }
//            break;
//        }
//
//        return result.toString();
    }

    public MultipartFile uploadFileFromPC(MultipartFile file, String folderName) throws IOException{

//        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = "";  
        String uploadDir = folderName + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Could not save image file: " + fileName, ioe);
        }

//        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
//        user.setPhotos(fileName);

//        User savedUser = repo.save(user);
//
//        String uploadDir = "user-photos/";
//
//        FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

        return file;
    }

    public MultipartFile downloadFileFromURL(String urlString, String folderName) throws IOException{

        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        String filename = FilenameUtils.getName(url.getPath()) + ".wav";
        File download = new File(folderName, filename);

        ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
        FileOutputStream fos = new FileOutputStream(download);
        try {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } finally {
            fos.close();
        }

        MultipartFile multipartFile = new MockMultipartFile(folderName, new FileInputStream(new File(folderName + "/" + filename)));

        return multipartFile;

    }

    public void onedriveCredentials(){
        final String clientId = "f551be30-233f-4680-8e28-bcc3166671a3"; //f551be30-233f-4680-8e28-bcc3166671a3
        final String[] scope = {"files.readwrite.all", "onedrive.readwrite", "offline_access"};
        final String redirectURL = "/projectdetailfour";
        final String clientSecret = "Fa~jaC5dsX7sE3h.bMq21~y-NPmyo20x84"; //Fa~jaC5dsX7sE3h.bMq21~y-NPmyo20x84

//        client = new Client(clientId, scope, redirectURL, clientSecret, false);
//        client.login();

    }

//    public void getFileOnedrive(String path) throws ErrorResponseException, IOException {
////        FileItem file = client.getFile("");
////        file.downloadAsync(Paths.get(path));
//
//        FileItem file1 = client.getFile(new PathPointer(""));
//    }

    public void speechToText(MultipartFile file, String title, String date, String meetingId, String pdm, String folderName) throws
            InterruptedException, ExecutionException, SQLException, IOException {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription("aec055f84d7d491d808b453ee675c8c4","eastasia");
        speechConfig.enableDictation();
        speechConfig.setSpeechRecognitionLanguage("en-US");
        fromFile3(speechConfig, file, meetingId, pdm, folderName);
//        String text = fromFile(speechConfig, file);
//        processInput(text);
    }

    public void fromFile3(SpeechConfig speechConfig, MultipartFile file, String meetingId, String pdm, String folderName) throws
            InterruptedException, ExecutionException, SQLException ,IOException{
//        AudioConfig audioConfig = AudioConfig.fromWavFileInput(file.getOriginalFilename());
        AudioConfig audioConfig = AudioConfig.fromWavFileInput(file.getOriginalFilename());
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);

        AtomicReference<String> textFromSpeech = new AtomicReference<>("");
        {
            //Subscribes to Events
            recognizer.recognizing.addEventListener((s, e) -> {
//                System.out.println("RECOGNIZING : Text = " + e.getResult().getText());
            });

            recognizer.recognized.addEventListener((s, e) -> {
                if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                    System.out.println("RECOGNIZED : Text = " + e.getResult().getText());
                    textFromSpeech.set(e.getResult().getText());
                    try {
                        processInput(textFromSpeech, meetingId, pdm);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                else if (e.getResult().getReason() == ResultReason.NoMatch) {
                    System.out.println("NOMATCH : No speech could be recognized");
                }
            });

            recognizer.canceled.addEventListener((s, e) -> {
                System.out.println("CANCELED: Reason=" + e.getReason());

                if(e.getReason() == CancellationReason.Error){
                    System.out.println("CANCELED: ErrorCode=" + e.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + e.getErrorDetails());
                    System.out.println("CANCELED: Did you update the subscription info?");
                }
            });

            recognizer.sessionStarted.addEventListener((s, e) -> {
                System.out.println("\n      Session started event.");
            });

            recognizer.sessionStopped.addEventListener((s, e) -> {
                System.out.println("\n      Session stopped event.");
            });

            System.out.println("Say something ...");
            recognizer.startContinuousRecognitionAsync().get();

            System.out.println("Press any key to stop");
            new Scanner(System.in).nextLine();

            recognizer.stopContinuousRecognitionAsync().get();
        }

        speechConfig.close();
        audioConfig.close();
        recognizer.close();
    }
}
