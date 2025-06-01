package main.java.tts;

import javazoom.jl.player.Player;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TTSApp {

    private static final Map<String, String> LANGUAGES = new LinkedHashMap<>();
    private static final Logger log = Logger.getLogger(TTSApp.class.getName());

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

    // Declare buttons as instance variables
    private JButton convertBtn;
    private JButton saveBtn;
    private JButton clearBtn;
    private JButton playBtn;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TTSApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = createFrame();
        JPanel panel = createMainPanel();
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Add listeners AFTER all buttons are initialized
        addListeners();
    }

    private void addListeners() {
        convertBtn.addActionListener(e -> handleConvert());
        saveBtn.addActionListener(e -> handleSave());
        clearBtn.addActionListener(e -> handleClear());
        playBtn.addActionListener(e -> handlePlay());
    }


    private JFrame createFrame() {
        JFrame frame = new JFrame("Text-to-Speech Converter");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
        return frame;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Text-to-Speech Converter", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Add title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Text Field Panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(createTextFieldPanel(), gbc);

        // Language Select Panel
        gbc.gridy = 2;
        panel.add(createLangPanel(), gbc);

        // Play checkbox
        gbc.gridy = 3;
        panel.add(playCheckBox, gbc);

        // Buttons Panel
        gbc.gridy = 4;
        panel.add(createButtonsPanel(), gbc);

        return panel;
    }

    private JPanel createTextFieldPanel() {
        JPanel textFieldPanel = new JPanel();
        textFieldPanel.setLayout(new FlowLayout());

        JLabel label = new JLabel("Enter Text:");

        textFieldPanel.add(label);
        textFieldPanel.add(textField);

        return textFieldPanel;
    }

    private JPanel createLangPanel() {
        JPanel langPanel = new JPanel();
        langPanel.setLayout(new FlowLayout());

        JLabel langLabel = new JLabel("Select Language:");

        langPanel.add(langLabel);
        langPanel.add(langComboBox);

        return langPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());

        // Initialize the buttons as instance variables
        convertBtn = new JButton("Convert");
        saveBtn = new JButton("Save As...");
        clearBtn = new JButton("Clear");
        playBtn = new JButton("Play");

        // Add buttons to the panel
        buttonsPanel.add(convertBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(clearBtn);
        buttonsPanel.add(playBtn);

        log.info("Count :{}" + buttonsPanel.getComponentCount());


        return buttonsPanel;
    }


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
            // Enable the play button after conversion
            playBtn.setEnabled(true);
        } catch (TTSException ex) {
            log.log(Level.SEVERE, "Error: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

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
                log.log(Level.SEVERE, "Error saving file: " + ex.getMessage(), ex);
                JOptionPane.showMessageDialog(null, "Error saving file: " + ex.getMessage());
            }
        }
    }

    private void handlePlay() {
        if (lastAudio != null) {
            try {
                playAudio(lastAudio);
            } catch (TTSException ex) {
                log.log(Level.SEVERE, "Playback error: " + ex.getMessage(), ex);
                JOptionPane.showMessageDialog(null, "Playback error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please convert text first.");
        }
    }

    private void handleClear() {
        textField.setText("");
        lastAudio = null;
        playBtn.setEnabled(false);  // Disable play button after clearing
    }

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

    private void playAudio(byte[] audioBytes) throws TTSException {
        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(audioBytes))) {
            Player player = new Player(bis);
            new Thread(() -> {
                try {
                    player.play();
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Audio playback failed.", ex);
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
