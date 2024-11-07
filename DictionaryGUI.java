
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import javax.swing.SwingConstants;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DictionaryGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textWord;
	private JTextField textMeaning;
	private JTextField textResponse;
    private DictionaryClient client;
	private DictionaryListGUI listGUI;
	private JSONParser parser = new JSONParser();

    public DictionaryGUI(DictionaryClient client) {
        this.client = client;
        initComponents();
    }

    	/**
	 * Create the frame.
	 */
    private void initComponents(){
        setTitle("Dictionary");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 861, 555);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 3, 0, 0));
		
		JLabel lblWord = new JLabel("Word");
		lblWord.setHorizontalAlignment(SwingConstants.CENTER);
		lblWord.setFont(new Font("Arial", Font.BOLD, 16));
		contentPane.add(lblWord);
		
		textWord = new JTextField();
		textWord.setFont(new Font("Arial", Font.BOLD, 16));
		contentPane.add(textWord);
		textWord.setColumns(10);
		
		JButton btnAddWord = new JButton("Add Word");
		btnAddWord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnAddWord.setFont(new Font("Arial", Font.BOLD, 16));
		contentPane.add(btnAddWord);
		
		JLabel lblMeaning = new JLabel("Meaning");
		lblMeaning.setHorizontalAlignment(SwingConstants.CENTER);
		lblMeaning.setFont(new Font("Arial", Font.BOLD, 16));
		contentPane.add(lblMeaning);
		
		textMeaning = new JTextField();
		textMeaning.setFont(new Font("Arial", Font.PLAIN, 14));
		contentPane.add(textMeaning);
		textMeaning.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.setFont(new Font("Arial", Font.BOLD, 16));
		contentPane.add(btnSearch);
		
		JLabel label = new JLabel("");
		contentPane.add(label);
		
		JLabel label_1 = new JLabel("");
		contentPane.add(label_1);
		
		JButton btnRemoveWord = new JButton("Remove Word");
		btnRemoveWord.setFont(new Font("Arial", Font.BOLD, 16));
		contentPane.add(btnRemoveWord);
		
		JLabel lblResponse = new JLabel("Response");
		lblResponse.setHorizontalAlignment(SwingConstants.CENTER);
		lblResponse.setFont(new Font("Arial", Font.BOLD, 16));
		contentPane.add(lblResponse);
		
		textResponse = new JTextField();
		textResponse.setFont(new Font("Arial", Font.PLAIN, 14));
		contentPane.add(textResponse);
		textResponse.setColumns(10);
		
		JLabel label_2 = new JLabel("");
		contentPane.add(label_2);
		
		JButton btnViewList = new JButton("View All Words");
		btnViewList.setFont(new Font("Arial", Font.BOLD, 16));
		btnViewList.addActionListener(e -> openDictionaryListGUI());
		contentPane.add(btnViewList);

		JButton btnAddMeaning = new JButton("Add Meaning");
        btnAddMeaning.setFont(new Font("Arial", Font.BOLD, 16));
        btnAddMeaning.addActionListener(e -> addMeaning());
        contentPane.add(btnAddMeaning);

        JButton btnUpdateMeaning = new JButton("Update Meaning");
        btnUpdateMeaning.setFont(new Font("Arial", Font.BOLD, 16));
        btnUpdateMeaning.addActionListener(e -> updateMeaning());
        contentPane.add(btnUpdateMeaning);

        btnAddWord.addActionListener(e -> addWord());
        btnSearch.addActionListener(e -> searchWord());
        btnRemoveWord.addActionListener(e -> removeWord());
    }

    private void addWord() {
		try {
			String word = textWord.getText().trim();
			String meaning = textMeaning.getText().trim();
			if(!word.isEmpty() && !meaning.isEmpty()) {
				String response = client.addWord(word, meaning);
				textResponse.setText(response);
			} else {
				textResponse.setText("Word and meaning cannot be empty");
			}
		} catch(IOException e) {
			textResponse.setText("Error: " + e.getMessage());
		}
    }

    private void searchWord() {
        try {
			String word = textWord.getText().trim();
			if(!word.isEmpty()) {
				String response = client.searchWord(word);
				textResponse.setText(response);
			} else {
				textResponse.setText("Word cannot be empty");
			}
		} catch(IOException e) {
			textResponse.setText("Error: " + e.getMessage());
		}
    }

    private void removeWord() {
        try {
			String word = textWord.getText().trim();
			if(!word.isEmpty()) {
				String response = client.removeWord(word);
				textResponse.setText(response);
			} else {
				textResponse.setText("Word cannot be empty");
			}
		} catch(IOException e) {
			textResponse.setText("Error: " + e.getMessage());
		}
    }

	private void openDictionaryListGUI() {
		if (listGUI == null || !listGUI.isDisplayable()) {
			listGUI = new DictionaryListGUI(client);
		}
		listGUI.setVisible(true);
		listGUI.refreshDictionaryList();
	}

	private void addMeaning() {
		try {
			String word = textWord.getText().trim();
			String newMeaning = textMeaning.getText().trim();
			if (!word.isEmpty() && !newMeaning.isEmpty()) {
				String response = client.addMeaning(word, newMeaning);
				textResponse.setText(response);
			} else {
				textResponse.setText("Word and meaning cannot be empty");
			}
		} catch (IOException e) {
			textResponse.setText("Error: " + e.getMessage());
		}
	}

	private void updateMeaning() {
        try {
			String word = textWord.getText().trim();
			String oldMeaning = client.searchWord(word);
			String newMeaning = textMeaning.getText().trim();
			if (!word.isEmpty() && oldMeaning != null && !oldMeaning.isEmpty() && !newMeaning.isEmpty()) {
				String response = client.updateMeaning(word, oldMeaning, newMeaning);
				textResponse.setText(response);
			} else {
				textResponse.setText("Word, old meaning, and new meaning cannot be empty");
			}
		} catch (IOException e) {
			textResponse.setText("Error: " + e.getMessage());
		}
	}
}