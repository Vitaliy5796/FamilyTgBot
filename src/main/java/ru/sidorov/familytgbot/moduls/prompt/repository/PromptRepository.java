package ru.sidorov.familytgbot.moduls.prompt.repository;

import org.springframework.stereotype.Repository;
import ru.sidorov.familytgbot.moduls.prompt.BaseRepository;
import ru.sidorov.familytgbot.moduls.prompt.entity.Prompt;

@Repository
public interface PromptRepository extends BaseRepository<Prompt> {
}
