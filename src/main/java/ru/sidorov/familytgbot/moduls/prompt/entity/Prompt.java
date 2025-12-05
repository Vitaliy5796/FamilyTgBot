package ru.sidorov.familytgbot.moduls.prompt.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "prompts", schema = "family")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Prompt {

    // Уникальный идентификатор
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Тема запроса
    @Column(name = "title")
    private String title;

    // Текст запроса для генерации
    @Column(name = "prompt")
    private String prompt;
}
