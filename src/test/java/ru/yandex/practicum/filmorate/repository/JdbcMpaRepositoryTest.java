package ru.yandex.practicum.filmorate.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(JdbcMpaRepository.class)
@DisplayName("JdbcMpaRepositoryTest")
class JdbcMpaRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcMpaRepository mpaRepository;

    private Mpa mpa1;
    private Mpa mpa2;
    private Mpa mpa3;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM mpa");

        jdbcTemplate.update("INSERT INTO mpa (mpa_id, mpa_name) VALUES (1, 'G')");
        jdbcTemplate.update("INSERT INTO mpa (mpa_id, mpa_name) VALUES (2, 'PG')");
        jdbcTemplate.update("INSERT INTO mpa (mpa_id, mpa_name) VALUES (3, 'PG-13')");

        mpa1 = new Mpa(1L, "G");
        mpa2 = new Mpa(2L, "PG");
        mpa3 = new Mpa(3L, "PG-13");
    }

    @Test
    @DisplayName("Должен находить MPA по существующему ID")
    void shouldFindMpaById() {
        Mpa foundMpa = mpaRepository.findById(1L);

        assertThat(foundMpa)
                .usingRecursiveComparison()
                .isEqualTo(mpa1);
    }

    @Test
    @DisplayName("Должен находить другой MPA по ID")
    void shouldFindAnotherMpaById() {
        Mpa foundMpa = mpaRepository.findById(3L);

        assertThat(foundMpa)
                .usingRecursiveComparison()
                .isEqualTo(mpa3);
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при поиске несуществующего MPA")
    void shouldThrowWhenFindingNonExistingMpa() {
        assertThatThrownBy(() -> mpaRepository.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("MPA рейтинг не найден");
    }

    @Test
    @DisplayName("Должен возвращать все MPA")
    void shouldReturnAllMpa() {
        Collection<Mpa> allMpa = mpaRepository.mpaGetAll();

        assertThat(allMpa)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(mpa1, mpa2, mpa3));
    }

    @Test
    @DisplayName("Должен возвращать корректные данные при пустой таблице")
    void shouldHandleEmptyTable() {
        jdbcTemplate.update("DELETE FROM mpa");

        Collection<Mpa> allMpa = mpaRepository.mpaGetAll();
        assertThat(allMpa).isEmpty();

        assertThatThrownBy(() -> mpaRepository.findById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("MPA рейтинг не найден");
    }

    @Test
    @DisplayName("Должен возвращать полный список MPA после добавления")
    void shouldReturnAllMpaAfterInsert() {
        jdbcTemplate.update("INSERT INTO mpa (mpa_id, mpa_name) VALUES (4, 'R')");

        Collection<Mpa> allMpa = mpaRepository.mpaGetAll();

        assertThat(allMpa)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(
                        mpa1,
                        mpa2,
                        mpa3,
                        new Mpa(4L, "R")
                ));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать поиск после удаления MPA")
    void shouldHandleFindAfterDelete() {
        jdbcTemplate.update("DELETE FROM mpa WHERE mpa_id = 2");

        assertThat(mpaRepository.mpaGetAll())
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(mpa1, mpa3));

        assertThatThrownBy(() -> mpaRepository.findById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("MPA рейтинг не найден");
    }
}