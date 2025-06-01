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

public class TTSApp {

    private static final Map<String, String> LANGUAGES = new LinkedHashMap<>() {{
        put("English", "en");
        put("Spanish", "es");
        put("French", "fr");
        put("German", "de");
        put("Italian", "it");
        put("Japanese", "ja");
        put("Korean", "ko");
        put("Portuguese", "pt");
        put("Russian", "ru");
        put("Chinese", "zh");
    }};

    public static void main(String[] args) {
        JFrame frame = new JFrame("Text to Speech Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Enter Text:");
        JTextField textField = new JTextField(40);

        JLabel langLabel = new JLabel("Select Language:");
        JComboBox<String> langComboBox = new JComboBox<>(LANGUAGES.keySet().toArray(new String[0]));

        JCheckBox playCheckBox = new JCheckBox("Pronunciation (Play immediately)");

        JButton convertBtn = new JButton("Convert");
        JButton saveBtn = new JButton("Save As...");
        JButton clearBtn = new JButton("Clear");

        frame.add(label);
        frame.add(textField);
        frame.add(langLabel);
        frame.add(langComboBox);
        frame.add(playCheckBox);
        frame.add(convertBtn);
        frame.add(saveBtn);
        frame.add(clearBtn);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        convertBtn.addActionListener(e -> {
            String text = textField.getText();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter some text.");
                return;
            }
            String selectedLang = LANGUAGES.get(langComboBox.getSelectedItem());

            try {
                byte[] mp3Data = fetchTTS(text, selectedLang);
                if (playCheckBox.isSelected()) {
                    playAudio(mp3Data);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });

        saveBtn.addActionListener(e -> {
            String text = textField.getText();
            String selectedLang = LANGUAGES.get(langComboBox.getSelectedItem());

            try {
                byte[] mp3Data = fetchTTS(text, selectedLang);
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(mp3Data);
                        JOptionPane.showMessageDialog(frame, "Saved successfully.");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });

        clearBtn.addActionListener(e -> textField.setText(""));
    }

    private static byte[] fetchTTS(String text, String lang) throws Exception {
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
    }

    private static void playAudio(byte[] audioBytes) throws Exception {
        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(audioBytes))) {
            Player player = new Player(bis);
            new Thread(() -> {
                try {
                    player.play();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();

        }
    }
}
