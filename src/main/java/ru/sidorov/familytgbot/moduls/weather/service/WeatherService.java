package ru.sidorov.familytgbot.moduls.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WebClient webClient = WebClient.create("https://api.open-meteo.com");

    // Координаты городов (можно вынести в application.properties)
    private record City(String name, double lat, double lon) {}

    private static final List<City> CITIES = List.of(
            new City("Орёл",          52.97, 36.07),
            new City("Санкт-Петербург", 59.93, 30.31)
    );

    public String getTodayForecast() {
        StringBuilder sb = new StringBuilder();
        sb.append("Прогноз погоды на сегодня:\n\n");

        for (City city : CITIES) {
            String json = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", city.lat)
                            .queryParam("longitude", city.lon)
                            .queryParam("current", "temperature_2m,weathercode,wind_speed_10m")
                            .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_probability_max,weathercode")
                            .queryParam("timezone", "Europe/Moscow")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // или сделай асинхронно

            String forecast = parseOpenMeteoJson(json, city.name);
            sb.append(forecast).append("\n");
        }
        return sb.toString().trim();
    }

    private String parseOpenMeteoJson(String json, String cityName) {
        try {
            JsonNode root = new ObjectMapper().readTree(json);

            JsonNode current = root.path("current");
            double tempNow = current.path("temperature_2m").asDouble();
            int codeNow = current.path("weathercode").asInt();

            JsonNode daily = root.path("daily");
            double tempMax = daily.path("temperature_2m_max").get(0).asDouble();
            double tempMin = daily.path("temperature_2m_min").get(0).asDouble();
            int precip = daily.path("precipitation_probability_max").get(0).asInt();

            String emojiNow = weatherCodeToEmoji(codeNow);
            String desc = weatherCodeToText(codeNow);

            return String.format("<b>%s</b>  %s\n" +
                            "<b>Сейчас:</b>  %.0f°C %s\n" +
                            "<b>Днём:</b>  %.0f°C / Ночью: %.0f°C\n" +
                            "<b>Осадки:</b>  %d%% \n\n",
                    cityName, emojiNow,
                    tempNow, desc,
                    tempMax, tempMin,
                    precip);

        } catch (Exception e) {
            log.error("Ошибка парсинга погоды для {}", cityName, e);
            return String.format("<b>%s</b>: не удалось получить прогноз\n", cityName);
        }
    }

    // WMO weather codes → эмодзи + короткое описание
    private String weatherCodeToEmoji(int code) {
        return switch (code) {
            case 0 -> "Ясно";
            case 1, 2, 3 -> "Облачно";
            case 45, 48 -> "Туман";
            case 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "Дождь";
            case 71, 73, 75, 77, 85, 86 -> "Снег";
            case 95, 96, 99 -> "Гроза";
            default -> "Погода";
        };
    }

    private String weatherCodeToText(int code) {
        return switch (code) {
            case 0 -> "ясно";
            case 1, 2 -> "малооблачно";
            case 3 -> "облачно";
            case 45, 48 -> "туман";
            case 51, 53, 55 -> "морось";
            case 61, 63, 65 -> "дождь";
            case 71, 73, 75 -> "снег";
            case 95, 96, 99 -> "гроза";
            default -> "";
        };
    }
}
