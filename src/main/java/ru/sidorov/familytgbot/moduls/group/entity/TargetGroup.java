package ru.sidorov.familytgbot.moduls.group.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "target_groups", schema = "family")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetGroup {

    @Id
    @Column(name = "chat_id")
    private Long chatId;               // Telegram chat_id (для групп отрицательный)

    @Column(name = "title")
    private String title;              // Название группы

    @Column(name = "is_active")
    private boolean isActive = true;     // Можно отключать рассылку в отдельные группы

    @Column(name = "custom_prompt")
    private String customPrompt;       // Можно хранить отдельный промпт для каждой группы
}
