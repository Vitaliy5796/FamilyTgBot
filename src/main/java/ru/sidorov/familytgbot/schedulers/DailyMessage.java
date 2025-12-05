package ru.sidorov.familytgbot.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sidorov.familytgbot.config.TgBot;
import ru.sidorov.familytgbot.moduls.group.entity.TargetGroup;
import ru.sidorov.familytgbot.moduls.group.repository.TargetGroupRepository;
import ru.sidorov.familytgbot.moduls.weather.service.WeatherService;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DailyMessage {

    private final TargetGroupRepository groupRepository;
    private final WeatherService weatherService;
    private final TgBot tgBot;

    public DailyMessage(TargetGroupRepository groupRepository,
                        WeatherService weatherService,
                        TgBot tgBot
    ) {
        this.groupRepository = groupRepository;
        this.weatherService = weatherService;
        this.tgBot = tgBot;
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void goodMorning() {
        String message = "Всем доброе утро и хорошего настроения! ☀\uFE0F";
        String weather = weatherService.getTodayForecast();

        getAllGroupIds().forEach(groupId -> {
            try {
                tgBot.sendDailyMessage(message, null, groupId);
                tgBot.sendMessageWithFormat(groupId, weather);
            } catch (Exception e) {
                log.error("===== Не удалось отправить утреннее сообщение в группу с id: {} ====", groupId, e);
            }
        });
    }

    @Scheduled(cron = "0 0 22 * * ?")
    public void goodNight() {
        String message = "Всем доброй ночи и хороших снов! \uD83C\uDF19";

        getAllGroupIds().forEach(groupId -> {
            try {
                tgBot.sendDailyMessage(message, null, groupId);
            } catch (Exception e) {
                log.error("===== Не удалось отправить вечернее сообщение в группу с id: {} ====", groupId, e);
            }
        });
    }

    private Set<Long> getAllGroupIds() {
        return groupRepository.findAll().stream()
                .filter(TargetGroup::isActive)
                .map(TargetGroup::getChatId)
                .collect(Collectors.toSet());
    }
}
