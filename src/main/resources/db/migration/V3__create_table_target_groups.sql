CREATE TABLE IF NOT EXISTS family.target_groups
(
    chat_id     BIGINT PRIMARY KEY,
    title  VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL,
    custom_prompt VARCHAR(255)
);