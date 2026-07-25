package com.moodjournal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoodJournalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoodJournalApplication.class, args);
    }
}
