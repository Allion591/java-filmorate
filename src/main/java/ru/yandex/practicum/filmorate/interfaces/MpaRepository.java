package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface MpaRepository {

    public Mpa findById(Long mpaId);

    public Collection<Mpa> mpaGetAll();
}