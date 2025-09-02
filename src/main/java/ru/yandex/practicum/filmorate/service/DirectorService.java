package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.interfaces.DirectorRepository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Service
@AllArgsConstructor
public class DirectorService {

    DirectorRepository directorRepository;

    public Collection<Director> findAll() {
        return directorRepository.findAll();
    }

    public Director getDirectorById(Long id) {
        return directorRepository.findById(id);
    }

    public Director createDirector(Director director) {
        return directorRepository.createDirector(director);
    }

    public Director updateDirector(Director director) {
        getDirectorById(director.getId());
        return directorRepository.update(director);
    }

    public void deleteDirector(Long id) {
        directorRepository.deleteDirector(id);
    }
}
