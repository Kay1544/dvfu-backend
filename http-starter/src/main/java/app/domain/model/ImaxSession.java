package app.domain.model;

import app.domain.exception.ConflictException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ImaxSession extends Session {
    public ImaxSession(long id, long movieId, Instant startTime, double price) {
        super(id, movieId, startTime, price);
    }

    @Override
    public HallType getHallType() { return HallType.IMAX; }

    @Override
    public int getCapacity() { return 30; }

    @Override
    public void validateBuy(int count, Instant now) {
        if (getStartTime().isBefore(now)) {
            throw new ConflictException("Session has already started");
        }
        if (getStartTime().isBefore(now.plus(15, ChronoUnit.MINUTES))) {
            throw new ConflictException("IMAX tickets can only be bought at least 15 minutes before start");
        }
    }
}