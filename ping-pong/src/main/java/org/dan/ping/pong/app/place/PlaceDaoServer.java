package org.dan.ping.pong.app.place;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static ord.dan.ping.pong.jooq.Tables.CITY;
import static ord.dan.ping.pong.jooq.Tables.PLACE;
import static ord.dan.ping.pong.jooq.Tables.PLACE_ADMIN;
import static ord.dan.ping.pong.jooq.tables.Tables.TABLES;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.extern.slf4j.Slf4j;
import ord.dan.ping.pong.jooq.tables.Place;
import ord.dan.ping.pong.jooq.tables.records.PlaceRecord;
import org.dan.ping.pong.app.city.CityLink;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.Uid;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class PlaceDaoServer implements PlaceDao {
    private static final String AUTHOR = "author";
    private static final String TBL_COUNT = "tbl-count";

    @Inject
    private DSLContext jooq;

    @Override
    @Transactional(TRANSACTION_MANAGER)
    public Pid create(String name, PlaceAddress address) {
        try {
            return jooq.insertInto(PLACE, PLACE.NAME, PLACE.CITY_ID,
                    PLACE.POST_ADDRESS, PLACE.PHONE, PLACE.EMAIL)
                    .values(name, address.getCity().getId(),
                            address.getAddress(),
                            address.getPhone(),
                            address.getEmail())
                    .returning(PLACE.PID)
                    .fetchOne()
                    .get(PLACE.PID);
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                if (e.getCause().getMessage().contains("Duplicate entry")) {
                    throw badRequest("Place with such name is already exist");
                }
            }
            throw e;
        }
    }

    @Override
    @Transactional(TRANSACTION_MANAGER)
    public Pid createAndGrant(Uid author, String name, PlaceAddress address) {
        final Pid pid = create(name, address);
        jooq.insertInto(PLACE_ADMIN, PLACE_ADMIN.PID, PLACE_ADMIN.UID, PLACE_ADMIN.TYPE)
                .values(pid, author, AUTHOR)
                .execute();
        log.info("Place {}/{} is created by {}", pid, name, author);
        return pid;
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<PlaceLink> findEditableByUid(Uid uid) {
        return jooq
                .select(PLACE.PID, PLACE.NAME,
                        PLACE.POST_ADDRESS,
                        PLACE.CITY_ID, CITY.NAME)
                .from(PLACE)
                .innerJoin(CITY).on(PLACE.CITY_ID.eq(CITY.CITY_ID))
                .innerJoin(PLACE_ADMIN)
                .on(PLACE.PID.eq(PLACE_ADMIN.PID))
                .where(PLACE_ADMIN.UID.eq(uid).and(PLACE_ADMIN.TYPE.in(AUTHOR)))
                .stream()
                .map(r -> PlaceLink.builder()
                        .name(r.getValue(PLACE.NAME))
                        .pid(r.getValue(PLACE.PID))
                        .address(PlaceAddress.builder()
                                .city(CityLink.builder()
                                        .id(r.get(PLACE.CITY_ID))
                                        .name(r.get(CITY.NAME))
                                        .build())
                                .address(r.getValue(PLACE.POST_ADDRESS))
                                .build())
                        .build())
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<PlaceInfoCountTables> getPlaceById(Pid pid) {
        return ofNullable(
                jooq
                        .select(PLACE.PID, PLACE.NAME, PLACE.POST_ADDRESS,
                                PLACE.PHONE, PLACE.EMAIL, PLACE.CITY_ID, CITY.NAME,
                                DSL.selectCount()
                                        .from(TABLES)
                                        .where(TABLES.PID.eq(PLACE.PID))
                                        .asField(TBL_COUNT))
                        .from(PLACE)
                        .innerJoin(CITY).on(PLACE.CITY_ID.eq(CITY.CITY_ID))
                        .where(PLACE.PID.eq(pid))
                        .fetchOne())
                .map(r -> PlaceInfoCountTables.builder()
                        .name(r.getValue(PLACE.NAME))
                        .pid(r.getValue(PLACE.PID))
                        .tables(r.getValue(TBL_COUNT, TABLES.TABLE_ID.getType()))
                        .address(PlaceAddress.builder()
                                .address(r.getValue(PLACE.POST_ADDRESS))
                                .city(CityLink.builder()
                                        .id(r.get(PLACE.CITY_ID))
                                        .name(r.get(CITY.NAME))
                                        .build())
                                .phone(r.get(PLACE.PHONE))
                                .email(r.get(PLACE.EMAIL))
                                .build())
                        .build());
    }

    @Override
    @Transactional(TRANSACTION_MANAGER)
    public void update(Uid uid, PlaceLink place) {
        log.info("User {} updates place {}", uid, place.getPid());
        final UpdateSetMoreStep<PlaceRecord> request = jooq.update(PLACE)
                .set(PLACE.NAME, place.getName())
                .set(PLACE.EMAIL, place.getAddress().getEmail())
                .set(PLACE.POST_ADDRESS, place.getAddress().getAddress())
                .set(PLACE.PHONE, place.getAddress().getPhone());
        ofNullable(place.getAddress().getCity())
                .ifPresent(city -> request.set(PLACE.CITY_ID, city.getId()));
        request.where(PLACE.PID.eq(place.getPid()))
                .execute();
    }

    private Set<Uid> loadAdmins(Pid pid) {
        return jooq.select(PLACE_ADMIN.UID)
                .from(PLACE_ADMIN).where(PLACE_ADMIN.PID.eq(pid))
                .fetch()
                .stream()
                .map(r -> r.get(PLACE_ADMIN.UID))
                .collect(toSet());
    }

    @Override
    public Optional<PlaceMemState> load(Pid pid) {
        return ofNullable(jooq.select(PLACE.NAME, PLACE.HOSTING_TID,
                PLACE.PHONE, PLACE.EMAIL, PLACE.GPS, PLACE.POST_ADDRESS,
                CITY.NAME, CITY.CITY_ID)
                .from(PLACE)
                .innerJoin(CITY).on(CITY.CITY_ID.eq(PLACE.CITY_ID))
                .where(PLACE.PID.eq(pid))
                .fetchOne())
                .map(r -> PlaceMemState.builder()
                        .pid(pid)
                        .hostingTid(r.get(PLACE.HOSTING_TID).map(Tid::new))
                        .name(r.get(PLACE.NAME))
                        .adminIds(loadAdmins(pid))
                        .address(PlaceAddress.builder()
                                .city(CityLink.builder()
                                        .id(r.get(CITY.CITY_ID))
                                        .name(r.get(CITY.NAME))
                                        .build())
                                .phone(r.get(PLACE.PHONE))
                                .address(r.get(PLACE.POST_ADDRESS))
                                .email(r.get(PLACE.EMAIL))
                                .gps(empty())
                                .build())
                        .build());
    }

    @Override
    public void setHostingTid(PlaceMemState place, DbUpdater batch) {
        batch.exec(
                DbUpdateSql
                        .builder()
                        .query(jooq.update(PLACE)
                                .set(PLACE.HOSTING_TID, place.getHostingTid()
                                        .map(Tid::getTid))
                                .where(PLACE.PID.eq(place.getPid())))
                        .build());
    }
}
