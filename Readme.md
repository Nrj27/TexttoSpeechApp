Sure! Here's a simple `README.md` for your Text-to-Speech (TTS) application. It explains how to set up, run, and use the project.

---

# Text-to-Speech Converter

A simple Java-based Text-to-Speech (TTS) application with support for multiple languages, speech speed adjustments, and an optional save functionality.

---

## Features

* **Text Input**: Convert any text to speech.
* **Language Selection**: Choose from multiple languages (e.g., English, Spanish, French, etc.).
* **Speed Control**: Adjust the speed of speech.
* **Play Audio**: Listen to the converted text without saving.
* **Save Audio**: Save the speech as an MP3 file.
* **User Interface**: Simple GUI built with JavaFX.

---

## Prerequisites

* **Java 17** or newer installed on your system.
* **Maven** for managing dependencies and building the project.
* **JLayer (1.0.1)** for MP3 playback.

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-repository/TextToSpeechApp.git
cd TextToSpeechApp
```

### 2. Build the Project

Make sure you have Maven installed. You can check with:

```bash
mvn -v
```

Then, build the project and package it into a **runnable JAR**:

```bash
mvn clean compile assembly:single
```

This will generate a `jar` file in the `target/` directory, like:

```
target/TextToSpeechApp-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 3. Run the Application

You can now run the application by executing:

```bash
java -jar target/TextToSpeechApp-1.0-SNAPSHOT-jar-with-dependencies.jar
```

---

## Using the Application

### GUI Overview

* **Text Field**: Type the text you want to convert into speech.
* **Language Selector**: Choose the language for the TTS.
* **Speech Speed**: Control how fast or slow the speech is.
* **Pronunciation Checkbox**: Optional pronunciation toggle.
* **Convert Button**: Converts text to speech.
* **Play Button**: Plays the converted speech.
* **Clear Button**: Clears the text input field.

### Save Audio (Optional)

You can choose to **save** the generated speech by selecting the option, or you can just listen to it immediately by clicking the **Play** button.

---

## Troubleshooting

### Common Errors

#### 1. **Incompatible thrown types in functional expression**

This happens when calling the `player.play()` method inside a lambda. The fix is to handle the checked exception properly:

```java
new Thread(() -> {
    try {
        player.play();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}).start();
```

#### 2. **Maven Dependency Not Found**

Make sure that you have added the following dependency in your `pom.xml` to use the `JLayer` library for MP3 playback:

```xml
<dependency>
    <groupId>javazoom</groupId>
    <artifactId>jlayer</artifactId>
    <version>1.0.1</version>
</dependency>
```

If Maven can't find the dependency, try running:

```bash
mvn clean install
```

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
