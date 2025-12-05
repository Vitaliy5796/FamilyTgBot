-- Этот скрипт должен подключаться к дефолтной БД (обычно postgres или template1)

-- Проверяем, существует ли база family
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'family') THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE family OWNER postgres ENCODING ''UTF8''');
END IF;
END $$;