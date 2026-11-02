package app.domain.service;

import app.domain.exception.ConflictException;
import app.domain.exception.NotFoundException;
import app.domain.exception.ValidationException;
import app.domain.model.Movie;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MovieService {
    private final Map<Long, Movie> movies = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final SessionService sessionService;

    public MovieService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public Movie create(String title, int durationMinutes) {
        if (title == null || title.isBlank()) throw new ValidationException("Title cannot be empty");
        if (durationMinutes <= 0) throw new ValidationException("Duration must be positive");
        Movie movie = new Movie(nextId.getAndIncrement(), title, durationMinutes);
        movies.put(movie.getId(), movie);
        return movie;
    }

    public List<Movie> findAll(String titleFilter) {
        return movies.values().stream()
                .filter(m -> titleFilter == null || m.getTitle().toLowerCase().contains(titleFilter.toLowerCase()))
                .sorted(Comparator.comparingLong(Movie::getId))
                .collect(Collectors.toList());
    }

    public Movie findById(long id) {
        Movie m = movies.get(id);
        if (m == null) throw new NotFoundException("Movie with id " + id + " not found");
        return m;
    }

    public Movie update(long id, String title, int durationMinutes) {
        Movie m = findById(id);
        if (title == null || title.isBlank()) throw new ValidationException("Title cannot be empty");
        if (durationMinutes <= 0) throw new ValidationException("Duration must be positive");
        m.setTitle(title);
        m.setDurationMinutes(durationMinutes);
        return m;
    }

    public void delete(long id) {
        findById(id);
        if (sessionService.existsByMovieId(id)) {
            throw new ConflictException("Cannot delete movie with existing sessions");
        }
        movies.remove(id);
    }
}