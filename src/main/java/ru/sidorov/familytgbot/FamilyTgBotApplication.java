package ru.sidorov.familytgbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FamilyTgBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyTgBotApplication.class, args);
    }

}
