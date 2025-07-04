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
    public static class DurationDeserializer extends JsonDeserializer<Duration> {
        @Override
        public Duration deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            long minutes = p.getLongValue();
            return Duration.ofMinutes(minutes);
        }
    }

    public static class DurationSerializer extends JsonSerializer<Duration> {
        @Override
        public void serialize(Duration value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(value.toMinutes());
        }
    }
}