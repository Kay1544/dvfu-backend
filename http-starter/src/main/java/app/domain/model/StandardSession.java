package app.domain.model;

import app.domain.exception.ConflictException;
import java.time.Instant;

public class StandardSession extends Session {
    public StandardSession(long id, long movieId, Instant startTime, double price) {
        super(id, movieId, startTime, price);
    }

    @Override
    public HallType getHallType() { return HallType.STANDARD; }

    @Override
    public int getCapacity() { return 50; }

    @Override
    public void validateBuy(int count, Instant now) {
        if (getStartTime().isBefore(now)) {
            throw new ConflictException("Session has already started");
        }
    }
}