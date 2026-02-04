import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// STEP 4 HTTP imports
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class InteractiveChatApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatWindow::new);
    }
}

/** ===================== MODEL ===================== */
/** Stores 1 buyer message + 3 seller rows */
class Message {
    private final String userMessage;           // buyer query, ex: "nasi padang"
    private final List<String> rowInputs;       // seller menu inputs

    public Message(String userMessage, int rowCount) {
        this.userMessage = userMessage;
        this.rowInputs = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) rowInputs.add("");
    }

    public String getUserMessage() { return userMessage; }

    public void setRowInput(int index, String value) { rowInputs.set(index, value); }

    public String getRowInput(int index) { return rowInputs.get(index); }
}

/** ===================== VIEW COMPONENT ===================== */
/** 1 input row: Label + TextField + Save Button */
class InputRow extends JPanel {
    private final JTextField textField = new JTextField();
    private final JButton actionButton;

    public interface SaveHandler {
        void onSave(int rowNumber, String content);
    }

    public InputRow(String labelText, int rowNumber, SaveHandler handler) {
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);

        add(new JLabel(labelText), BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);

        actionButton = new JButton("Save " + rowNumber);
        actionButton.addActionListener(e -> handler.onSave(rowNumber, textField.getText().trim()));
        add(actionButton, BorderLayout.EAST);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    public void setText(String value) { textField.setText(value); }
}

/** 1 message block: header + 3 InputRow */
class MessageBlock extends JPanel {
    private final Message message;

    public MessageBlock(Message message, InputRow.SaveHandler handler) {
        this.message = message;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        setBackground(Color.WHITE);

        JLabel header = new JLabel("<html><b>User Sent:</b> " + escHtml(message.getUserMessage()) + "</html>");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(header);
        add(Box.createRigidArea(new Dimension(0, 15)));

        for (int i = 1; i <= 3; i++) {
            InputRow row = new InputRow("Input " + i + ": ", i, handler);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(row);
            add(Box.createRigidArea(new Dimension(0, 8)));
        }

        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }

    public Message getMessage() { return message; }

    private static String escHtml(String s){
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}

/** ===================== API CLIENT ===================== */
/**
 * Sends seller offers to Spring Boot:
 * POST http://localhost:8080/api/offers
 */
class SellerApi {
    private final String baseUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    public SellerApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void createOffer(String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/offers"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }
}

/** ===================== MAIN WINDOW ===================== */
/** ===================== MAIN WINDOW (UPDATED) ===================== */
/** ===================== MAIN WINDOW (UPDATED WITH SCORE) ===================== */
class ChatWindow extends JFrame {
    private final JPanel chatContainer = new JPanel();
    private final JTextField mainInput = new JTextField();
    
    // Input Fields untuk fleksibilitas
    private final JTextField vendorInput = new JTextField("Seller B", 8);
    private final JTextField priceInput = new JTextField("10000", 6);
    private final JTextField scoreInput = new JTextField("42.0", 5); // Input Score Baru

    private final SellerApi api = new SellerApi("http://localhost:8080");

    // Defaults lainnya
    private final int defaultEtaMin = 10;
    private final int defaultSweet = 0;
    private final int defaultSimple = 2;
    private final boolean defaultActive = true;

    public ChatWindow() {
        setTitle("Multi-Button Interaction GUI (Seller -> Backend)");
        setSize(700, 750); // Ukuran sedikit diperlebar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(new Color(230, 230, 235));

        JScrollPane scrollPane = new JScrollPane(chatContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        add(createBottomBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createBottomBar() {
        JPanel mainBottomPanel = new JPanel();
        mainBottomPanel.setLayout(new BoxLayout(mainBottomPanel, BoxLayout.Y_AXIS));
        mainBottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Row Pengaturan: Vendor, Price, dan Score
        JPanel settingsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        settingsRow.add(new JLabel("Seller:"));
        settingsRow.add(vendorInput);
        
        settingsRow.add(Box.createRigidArea(new Dimension(10, 0)));
        settingsRow.add(new JLabel("Price:"));
        settingsRow.add(priceInput);

        settingsRow.add(Box.createRigidArea(new Dimension(10, 0)));
        settingsRow.add(new JLabel("Score:"));
        settingsRow.add(scoreInput); // Menambahkan kotak score ke UI
        
        // Row Chat
        JPanel chatRow = new JPanel(new BorderLayout(10, 0));
        chatRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton sendBtn = new JButton("Send Message");
        sendBtn.addActionListener(e -> postMessage());
        mainInput.addActionListener(e -> postMessage());

        chatRow.add(mainInput, BorderLayout.CENTER);
        chatRow.add(sendBtn, BorderLayout.EAST);

        mainBottomPanel.add(settingsRow);
        mainBottomPanel.add(chatRow);
        
        return mainBottomPanel;
    }

    private void postMessage() {
        String msg = mainInput.getText().trim();
        if (msg.isEmpty()) return;

        Message message = new Message(msg, 3);
        InputRow.SaveHandler handler = (rowNumber, content) -> handleSave(message, rowNumber, content);

        chatContainer.add(new MessageBlock(message, handler));
        chatContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        chatContainer.revalidate();
        chatContainer.repaint();
        mainInput.setText("");

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) chatContainer.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void handleSave(Message message, int rowNumber, String content) {
        if (content == null || content.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Input " + rowNumber + " kosong!");
            return;
        }

        // Ambil data dari semua input field
        String item = content.trim();
        String currentVendor = vendorInput.getText().trim();
        String currentPrice = priceInput.getText().trim().replace(".", ""); // Bersihkan titik
        String currentScore = scoreInput.getText().trim(); // Ambil nilai score
        
        message.setRowInput(rowNumber - 1, item);
        String category = message.getUserMessage().trim();
        if (category.isEmpty()) category = "unknown";

        // Susun JSON dengan menyertakan field "score"
        String json = "{"
                + "\"item\":\"" + escJson(item) + "\","
                + "\"vendor\":\"" + escJson(currentVendor) + "\","
                + "\"category\":\"" + escJson(category) + "\","
                + "\"price\":" + currentPrice + ","
                + "\"score\":" + currentScore + "," // Score dikirim ke backend
                + "\"etaMin\":" + defaultEtaMin + ","
                + "\"sweet\":" + defaultSweet + ","
                + "\"simple\":" + defaultSimple + ","
                + "\"active\":" + defaultActive
                + "}";

        new Thread(() -> {
            try {
                api.createOffer(json);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        "✅ Berhasil Upload!\n\n"
                                + "Item: " + item + "\n"
                                + "Vendor: " + currentVendor + "\n"
                                + "Price: Rp " + currentPrice + "\n"
                                + "Score: " + currentScore
                ));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this, "❌ Gagal Upload:\n" + ex.getMessage()
                ));
            }
        }).start();
    }

    private static String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}