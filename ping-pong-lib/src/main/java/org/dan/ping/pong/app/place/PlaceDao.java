package org.dan.ping.pong.app.place;

import org.dan.ping.pong.app.tournament.Uid;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.List;
import java.util.Optional;

public interface PlaceDao {
    int create(String name, PlaceAddress address);

    int createAndGrant(Uid author, String name, PlaceAddress address);

    List<PlaceLink> findEditableByUid(Uid uid);

    Optional<PlaceInfoCountTables> getPlaceById(int pid);

    void update(Uid uid, PlaceLink place);

    Optional<PlaceMemState> load(Pid pid);

    void setHostingTid(PlaceMemState place, DbUpdater batch);
}