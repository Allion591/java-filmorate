package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.interfaces.MpaRepository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Slf4j
@Repository
@Qualifier("jdbcMpaRepository")
@RequiredArgsConstructor
public class JdbcMpaRepository implements MpaRepository {
    private final NamedParameterJdbcOperations jdbcOperations;

    public Mpa findById(Long mpaId) {
        String sql = "SELECT * FROM mpa WHERE mpa_id = :mpa_id";
        try {
            log.info("Запрос в базу {}", mpaId);
            return jdbcOperations.queryForObject(
                    sql,
                    new MapSqlParameterSource("mpa_id", mpaId),
                    (rs, rowNum) -> new Mpa(
                            rs.getLong("mpa_id"),
                            rs.getString("mpa_name"))

            );
        } catch (EmptyResultDataAccessException e) {
            log.info("Ошибка {}", e.getMessage());
            throw new NotFoundException("MPA рейтинг не найден");
        }
    }

    public Collection<Mpa> mpaGetAll() {
        String sql = "SELECT * FROM mpa GROUP BY mpa_id";
        return jdbcOperations.query(
                sql, (rs, rowNum) -> new Mpa(
                        rs.getLong("mpa_id"),
                        rs.getString("mpa_name")
                ));
    }
}