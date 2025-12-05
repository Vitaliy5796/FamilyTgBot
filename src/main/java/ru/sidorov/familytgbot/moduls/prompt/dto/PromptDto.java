package ru.sidorov.familytgbot.moduls.prompt.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PromptDto {

    private String title;

    private String prompt;
}
