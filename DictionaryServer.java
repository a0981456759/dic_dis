import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DictionaryServer {
    private int port;
    private String dictionaryFile;
    private ConcurrentHashMap<String, String> dictionary;
    private ExecutorService threadPool;

    public DictionaryServer(int port, String dictionaryFile) {
        this.port = port;
        this.dictionaryFile = dictionaryFile;
        this.dictionary = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
    }

    public String getDicFile(){
        return this.dictionaryFile;
    }

    public void start() {
        loadDictionary();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Dictionary server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected"+clientSocket.getInetAddress().getHostAddress());
                threadPool.execute(new ClientHandler(clientSocket, dictionary, this));
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadDictionary() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(dictionaryFile)) {
            JSONObject jsonDictionary = (JSONObject) parser.parse(reader);
            dictionary = new ConcurrentHashMap<>();
            for (Object key : jsonDictionary.keySet()) {
                dictionary.put((String) key, (String) jsonDictionary.get(key));
            }
        } catch (IOException | ParseException e) {
            System.out.println("Error loading dictionary: " + e.getMessage());
            dictionary = new ConcurrentHashMap<>();
        }
    }

    public synchronized void saveDictionary() {
        try (FileWriter file = new FileWriter(dictionaryFile)) {
            JSONObject jsonDictionary = new JSONObject(dictionary);
            file.write(jsonDictionary.toJSONString());
            file.flush();
            System.out.println("Dictionary saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving dictionary: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DictionaryServer.jar <port> <dictionary-file>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String dictionaryFile = args[1];
        DictionaryServer server = new DictionaryServer(port, dictionaryFile);
        server.start();
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ConcurrentHashMap<String, String> dictionary;
    private DictionaryServer server;
    private JSONParser parser = new JSONParser();

    public ClientHandler(Socket clientSocket, ConcurrentHashMap<String, String> dictionary, DictionaryServer server) {
        this.clientSocket = clientSocket;
        this.dictionary = dictionary;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String line;
            while ((line = reader.readLine()) != null) {
                JSONObject json = (JSONObject) parser.parse(line);
                String command = (String) json.get("command");
                String response;

                switch(command) {
                    case "SEARCH":
                        response = searchWord((String) json.get("word"));
                        break;
                    case "ADD":
                        response = addWord((String) json.get("word"), (String) json.get("meaning"));
                        break;
                    case "REMOVE":
                        response = removeWord((String) json.get("word"));
                        break;
                    case "GETALL":
                        JSONObject dictJson = new JSONObject(dictionary);
                        JSONObject responseJson = new JSONObject();
                        responseJson.put("dictionary", (Object)dictJson);
                        writer.println(responseJson.toJSONString());
                        continue;
                    case "ADDMEANING":
                        response = addMeaning((String) json.get("word"), (String) json.get("meaning"));
                        break;
                    case "UPDATEMEANING":
                        response = updateMeaning((String) json.get("word"), (String) json.get("oldMeaning"), (String) json.get("newMeaning"));
                        break;
                    default:
                        response = "Invalid command";
                }

                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("response", (Object)response);
                writer.println(jsonResponse.toJSONString());
            }
        } catch (IOException | ParseException e) {
            System.out.println("Client disconnected");
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            }catch (IOException e){
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private String searchWord(String word) {
        String meaning = dictionary.get(word);
        return (meaning != null) ? meaning : "Word not found";
    }

    private String addWord(String word, String meaning) {
        String preWord = dictionary.putIfAbsent(word, meaning);
        String result = (preWord == null) ? "Word added successfully" : "Word already exists";
        if (preWord == null) {
            server.saveDictionary();
        }
        return result;
    }

    private String removeWord(String word) {
        String meaning = dictionary.remove(word);
        if (meaning != null) {
            server.saveDictionary();
            return "Word removed successfully";
        } else {
            return "Word not found";
    }
    }

    private String addMeaning(String word, String newMeaning) {
        String currentMeaning = dictionary.get(word);
        if (currentMeaning == null) {
            return "Word not found";
        }
        if (currentMeaning.contains(newMeaning)) {
            return "Meaning already exists";
        }
        dictionary.compute(word, (k, v) -> v + "; " + newMeaning);
        server.saveDictionary();
        return "Meaning added successfully";
    }

    private String updateMeaning(String word, String oldMeaning, String newMeaning) {
        String currentMeaning = dictionary.get(word);
        if (currentMeaning == null) {
            return "Word not found";
        }
        if (!currentMeaning.contains(oldMeaning)) {
            return "No new meaning";
        }
        String updatedMeaning = currentMeaning.replace(oldMeaning, newMeaning);
        dictionary.put(word, updatedMeaning);
        server.saveDictionary();
        return "Meaning updated successfully";
    }
}