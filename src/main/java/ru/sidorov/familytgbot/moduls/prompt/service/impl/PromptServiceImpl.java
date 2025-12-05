package ru.sidorov.familytgbot.moduls.prompt.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sidorov.familytgbot.moduls.prompt.dto.PromptDto;
import ru.sidorov.familytgbot.moduls.prompt.mapper.PromptMapper;
import ru.sidorov.familytgbot.moduls.prompt.repository.PromptRepository;
import ru.sidorov.familytgbot.moduls.prompt.service.abstracts.PromptService;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromptServiceImpl implements PromptService {

    PromptRepository promptRepository;

    @Override
    public void save(PromptDto promptDto) {
        if (promptDto == null
                || promptDto.getTitle() == null
                || promptDto.getTitle().isBlank()
                || promptDto.getPrompt() == null
                || promptDto.getPrompt().isBlank()) {
            log.error("Заголовок или промпт пустой");
            return;
        }

        promptRepository.save(PromptMapper.createNewPrompt(promptDto));
    }
}
