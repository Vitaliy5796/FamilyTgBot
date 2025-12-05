package ru.sidorov.familytgbot.moduls.prompt.mapper;

import ru.sidorov.familytgbot.moduls.prompt.dto.PromptDto;
import ru.sidorov.familytgbot.moduls.prompt.entity.Prompt;

public class PromptMapper {

    public static Prompt createNewPrompt(PromptDto promptDto) {
        return Prompt.builder()
                .title(promptDto.getTitle())
                .prompt(promptDto.getPrompt())
                .build();
    }
}
