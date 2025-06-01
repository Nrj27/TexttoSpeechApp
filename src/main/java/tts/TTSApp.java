package main.java.tts;

import javazoom.jl.player.Player;

import javax.swing.*;
import javax.swing.WindowConstants;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class TTSApp {

    private static final Map<String, String> LANGUAGES = new LinkedHashMap<>();

    static {
        LANGUAGES.put("English", "en");
        LANGUAGES.put("Spanish", "es");
        LANGUAGES.put("French", "fr");
        LANGUAGES.put("German", "de");
        LANGUAGES.put("Italian", "it");
        LANGUAGES.put("Japanese", "ja");
        LANGUAGES.put("Korean", "ko");
        LANGUAGES.put("Portuguese", "pt");
        LANGUAGES.put("Russian", "ru");
        LANGUAGES.put("Chinese", "zh");
    }

    private final JTextField textField = new JTextField(40);
    private final JComboBox<String> langComboBox = new JComboBox<>(LANGUAGES.keySet().toArray(new String[0]));
    private final JCheckBox playCheckBox = new JCheckBox("Pronunciation (Play immediately)");
    private byte[] lastAudio = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TTSApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = createFrame();
        JPanel panel = createMainPanel();
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Add the event listeners directly here
        addListeners(frame);
    }

    // Method to create the JFrame
    private JFrame createFrame() {
        JFrame frame = new JFrame("Text to Speech Converter");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new FlowLayout());
        return frame;
    }

    // Method to create main panel for layout
    private JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createTextFieldPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    // Method to create the text field panel
    private JPanel createTextFieldPanel() {
        JPanel textFieldPanel = new JPanel();
        textFieldPanel.setLayout(new FlowLayout());

        JLabel label = new JLabel("Enter Text:");
        JLabel langLabel = new JLabel("Select Language:");

        textFieldPanel.add(label);
        textFieldPanel.add(textField);
        textFieldPanel.add(langLabel);
        textFieldPanel.add(langComboBox);
        textFieldPanel.add(playCheckBox);

        return textFieldPanel;
    }

    // Method to create buttons panel
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());

        JButton convertBtn = new JButton("Convert");
        JButton saveBtn = new JButton("Save As...");
        JButton clearBtn = new JButton("Clear");
        JButton playBtn = new JButton("Play");

        buttonsPanel.add(convertBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(clearBtn);
        buttonsPanel.add(playBtn);

        return buttonsPanel;
    }

    // Add event listeners to buttons
    private void addListeners(JFrame frame) {
        // Event listener for Convert button
        JButton convertBtn = (JButton) frame.getComponentAt(0, 0);
        convertBtn.addActionListener(e -> handleConvert());

        // Event listener for Save button
        JButton saveBtn = (JButton) frame.getComponentAt(0, 0);
        saveBtn.addActionListener(e -> handleSave());

        // Event listener for Clear button
        JButton clearBtn = (JButton) frame.getComponentAt(0, 0);
        clearBtn.addActionListener(e -> handleClear());

        // Event listener for Play button
        JButton playBtn = (JButton) frame.getComponentAt(0, 0);
        playBtn.addActionListener(e -> handlePlay());
    }

    // Method to handle text conversion
    private void handleConvert() {
        String text = textField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter some text.");
            return;
        }

        String selectedLang = LANGUAGES.get(langComboBox.getSelectedItem());
        try {
            lastAudio = fetchTTS(text, selectedLang);
            if (playCheckBox.isSelected()) {
                playAudio(lastAudio);
            }
        } catch (TTSException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // Method to handle saving the audio
    private void handleSave() {
        if (lastAudio == null) {
            JOptionPane.showMessageDialog(null, "Please convert text first.");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(lastAudio);
                JOptionPane.showMessageDialog(null, "Saved successfully.");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saving file: " + ex.getMessage());
            }
        }
    }

    // Method to handle play action
    private void handlePlay() {
        if (lastAudio != null) {
            try {
                playAudio(lastAudio);
            } catch (TTSException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Playback error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please convert text first.");
        }
    }

    // Method to clear the text field
    private void handleClear() {
        textField.setText("");
        lastAudio = null;
    }

    // Method to fetch TTS data
    private byte[] fetchTTS(String text, String lang) throws TTSException {
        try {
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String urlStr = String.format("https://translate.google.com/translate_tts?ie=UTF-8&tl=%s&client=tw-ob&q=%s", lang, encodedText);
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (InputStream is = conn.getInputStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                return baos.toByteArray();
            }
        } catch (IOException ex) {
            throw new TTSException("Failed to fetch TTS data.", ex);
        }
    }

    // Method to play audio
    private void playAudio(byte[] audioBytes) throws TTSException {
        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(audioBytes))) {
            Player player = new Player(bis);
            new Thread(() -> {
                try {
                    player.play();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        } catch (Exception ex) {
            throw new TTSException("Audio playback failed.", ex);
        }
    }

    // Custom exception class
    public static class TTSException extends Exception {
        public TTSException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
