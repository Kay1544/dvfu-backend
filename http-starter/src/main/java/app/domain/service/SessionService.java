package app.domain.service;

import app.domain.exception.ConflictException;
import app.domain.exception.NotFoundException;
import app.domain.exception.ValidationException;
import app.domain.model.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class SessionService {
    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    private MovieService movieService;

    public void setMovieService(MovieService movieService) {
        this.movieService = movieService;
    }

    public boolean existsByMovieId(long movieId) {
        return sessions.values().stream().anyMatch(s -> s.getMovieId() == movieId);
    }

    public Session create(long movieId, Instant startTime, HallType type, double price) {
        if (movieService != null) {
            movieService.findById(movieId);
        }
        if (startTime == null) throw new ValidationException("Start time is required");
        if (price < 0) throw new ValidationException("Price cannot be negative");

        long id = nextId.getAndIncrement();
        Session session = type == HallType.IMAX
                ? new ImaxSession(id, movieId, startTime, price)
                : new StandardSession(id, movieId, startTime, price);
        sessions.put(id, session);
        return session;
    }

    public List<Session> findAll(Long movieId, HallType type, Boolean onlyAvailable) {
        return sessions.values().stream()
                .filter(s -> movieId == null || s.getMovieId() == movieId)
                .filter(s -> type == null || s.getHallType() == type)
                .filter(s -> onlyAvailable == null || !onlyAvailable || s.getSoldTickets() < s.getCapacity())
                .sorted(Comparator.comparingLong(Session::getId))
                .collect(Collectors.toList());
    }

    public Session findById(long id) {
        Session s = sessions.get(id);
        if (s == null) throw new NotFoundException("Session with id " + id + " not found");
        return s;
    }

    public void delete(long id) {
        findById(id);
        sessions.remove(id);
    }

    public Session buyTickets(long id, int count) {
        Session s = findById(id);
        Instant now = Instant.now();
        if (count <= 0) throw new ValidationException("Ticket count must be positive");

        s.validateBuy(count, now);
        if (s.getSoldTickets() + count > s.getCapacity()) {
            throw new ConflictException("Not enough seats available. Left: " + (s.getCapacity() - s.getSoldTickets()));
        }
        s.buyTickets(count);
        return s;
    }
}