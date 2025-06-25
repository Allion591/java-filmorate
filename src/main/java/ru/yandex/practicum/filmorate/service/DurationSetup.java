package ru.yandex.practicum.filmorate.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Duration;

public class DurationSetup {
    public class DurationDeserializer extends JsonDeserializer<Duration> {
        @Override
        public Duration deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            long seconds = p.getLongValue(); // Получаем число из JSON
            return Duration.ofSeconds(seconds); // Создаем Duration из секунд
        }
    }

    // Сериализатор: Duration -> число (секунды)
    public class DurationSerializer extends JsonSerializer<Duration> {
        @Override
        public void serialize(Duration value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(value.getSeconds()); // Преобразуем Duration в секунды (число)
        }
    }

}

