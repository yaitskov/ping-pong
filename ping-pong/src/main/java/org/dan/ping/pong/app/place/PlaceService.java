package org.dan.ping.pong.app.place;

import static org.dan.ping.pong.app.place.PlaceCacheFactory.PLACE_CACHE;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.match.Pid;
import org.dan.ping.pong.app.tournament.Cache;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

public class PlaceService implements Cache<Pid, PlaceMemState> {
    @Inject
    @Named(PLACE_CACHE)
    private LoadingCache<Pid, PlaceMemState> placeCache;

    public void invalidate(Pid pid) {
        placeCache.invalidate(pid);
    }

    @SneakyThrows
    public PlaceMemState load(Pid pid) {
        try {
            return placeCache.get(pid);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
}
