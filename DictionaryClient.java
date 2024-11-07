import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.SwingUtilities;

public class DictionaryClient {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JSONParser parser = new JSONParser();

    public DictionaryClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void connect() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connected to server at " + serverAddress + ":" + serverPort);
    }

    public String sendRequest (String request) throws IOException {
        out.println(request);
        return in.readLine();
    }

    public String getDic() throws IOException{
        return sendRequest("GETALL");
    }

    public void close() throws IOException {
        if(in != null) in.close();
        if(out != null) out.close();
        if(socket != null) socket.close();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public String searchWord(String word) throws IOException {
        JSONObject json = new JSONObject();
        json.put("command", "SEARCH");
        json.put("word", word);
        String response = sendRequest(json.toJSONString());
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(response);
            return (String) jsonResponse.get("response");
        } catch (ParseException e) {
            throw new IOException("Error parsing response: " + e.getMessage());
        }
    }

    public String addWord(String word, String meaning) throws IOException {
        JSONObject json = new JSONObject();
        json.put("command", "ADD");
        json.put("word", word);
        json.put("meaning", meaning);
        String response = sendRequest(json.toJSONString());
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(response);
            return (String) jsonResponse.get("response");
        } catch (ParseException e) {
            throw new IOException("Error parsing response: " + e.getMessage());
        }
    }

    public String removeWord(String word) throws IOException {
        JSONObject json = new JSONObject();
        json.put("command", "REMOVE");
        json.put("word", word);
        String response = sendRequest(json.toJSONString());
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(response);
            return (String) jsonResponse.get("response");
        } catch (ParseException e) {
            throw new IOException("Error parsing response: " + e.getMessage());
        }
    }

    public Map<String, String> getAllWords() throws IOException {
        JSONObject json = new JSONObject();
        json.put("command", "GETALL");
        String response = sendRequest(json.toJSONString());
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(response);
            JSONObject dictJson = (JSONObject) jsonResponse.get("dictionary");
            Map<String, String> dictionary = new HashMap<>();
            for (Object key : dictJson.keySet()) {
                dictionary.put((String) key, (String) dictJson.get(key));
            }
            return dictionary;
        } catch (ParseException e) {
            throw new IOException("Error parsing response: " + e.getMessage());
        }
    }

    public String getMeaning(String word) throws IOException {
        String meaning = null;
        String filename = getDic();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(word)) {
                    meaning =  parts[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading dictionary file: " + e.getMessage());
            throw e;
        }
        return meaning;
    }

    public String addMeaning(String word, String newMeaning) throws IOException {
        JSONObject json = new JSONObject();
        json.put("command", "ADDMEANING");
        json.put("word", word);
        json.put("meaning", newMeaning);
        String response = sendRequest(json.toJSONString());
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(response);
            return (String) jsonResponse.get("response");
        } catch (ParseException e) {
            throw new IOException("Error parsing response: " + e.getMessage());
        }
    }

    public String updateMeaning(String word, String oldMeaning, String newMeaning) throws IOException {
        JSONObject json = new JSONObject();
        json.put("command", "UPDATEMEANING");
        json.put("word", word);
        json.put("oldMeaning", oldMeaning);
        json.put("newMeaning", newMeaning);
        String response = sendRequest(json.toJSONString());
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(response);
            return (String) jsonResponse.get("response");
        } catch (ParseException e) {
            throw new IOException("Error parsing response: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DictionaryClient.jar <server-address> <server-port>");
            return;
        }

        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        DictionaryClient client = new DictionaryClient(serverAddress, serverPort);
        try {
            client.connect();
            SwingUtilities.invokeLater(() -> {
                DictionaryGUI gui = new DictionaryGUI(client);
                gui.setVisible(true);
            });
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}