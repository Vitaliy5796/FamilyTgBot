package ru.sidorov.familytgbot.moduls.prompt.service.abstracts;

import ru.sidorov.familytgbot.moduls.prompt.dto.PromptDto;

public interface PromptService {

    void save(PromptDto promptDto);
}
