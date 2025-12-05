package ru.sidorov.familytgbot.moduls.group.repository;

import org.springframework.stereotype.Repository;
import ru.sidorov.familytgbot.moduls.group.entity.TargetGroup;
import ru.sidorov.familytgbot.moduls.prompt.BaseRepository;

@Repository
public interface TargetGroupRepository extends BaseRepository<TargetGroup> {
}
