package app.domain.model;

import java.time.Instant;

public abstract class Session {
    private final long id;
    private final long movieId;
    private final Instant startTime;
    private final double price;
    private int soldTickets;

    protected Session(long id, long movieId, Instant startTime, double price) {
        this.id = id;
        this.movieId = movieId;
        this.startTime = startTime;
        this.price = price;
        this.soldTickets = 0;
    }

    public long getId() { return id; }
    public long getMovieId() { return movieId; }
    public Instant getStartTime() { return startTime; }
    public double getPrice() { return price; }
    public int getSoldTickets() { return soldTickets; }

    public abstract HallType getHallType();
    public abstract int getCapacity();
    
    // Полиморфизм: разные типы сеансов проверяют правила по-разному
    public abstract void validateBuy(int count, Instant now);

    public void buyTickets(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        if (soldTickets + count > getCapacity()) {
            throw new IllegalStateException("Not enough seats");
        }
        soldTickets += count;
    }
}