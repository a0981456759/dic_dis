import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DictionaryListGUI extends JFrame {
    private JTable tableDictionary;
    private DefaultTableModel tableModel;
    private DictionaryClient client;

    public DictionaryListGUI(DictionaryClient client) {
        this.client = client;
        initComponents();
        refreshDictionaryList();
    }

    private void initComponents() {
        setTitle("Dictionary List");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 600, 400);

        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        String[] columnNames = {"Word", "Meaning"};
        tableModel = new DefaultTableModel(columnNames, 0);
        tableDictionary = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableDictionary);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefresh.addActionListener(e -> refreshDictionaryList());
        contentPane.add(btnRefresh, BorderLayout.SOUTH);
    }

    public void refreshDictionaryList() {
        try {
            Map<String, String> dictionaryContent = client.getAllWords();
            updateTable(dictionaryContent);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error fetching dictionary content: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(Map<String, String> dictionary) {
        tableModel.setRowCount(0);
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
}