package org.dan.ping.pong.app.place;

import static java.util.stream.Collectors.toList;
import static ord.dan.ping.pong.jooq.Tables.PLACE;
import static ord.dan.ping.pong.jooq.Tables.PLACE_ADMIN;
import static ord.dan.ping.pong.jooq.tables.Tables.TABLES;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class PlaceDao {
    public static final String AUTHOR = "author";
    private static final String TBL_COUNT = "tbl-count";

    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public int create(String name, PlaceAddress address) {
        return jooq.insertInto(PLACE, PLACE.NAME, PLACE.POST_ADDRESS,
                PLACE.PHONE, PLACE.EMAIL)
                .values(name, address.getAddress(), address.getPhone(),
                        address.getEmail())
                .returning(PLACE.PID)
                .fetchOne()
                .get(PLACE.PID);
    }

    @Transactional(TRANSACTION_MANAGER)
    public int createAndGrant(int author, String name, PlaceAddress address) {
        final int pid = create(name, address);
        jooq.insertInto(PLACE_ADMIN, PLACE_ADMIN.PID, PLACE_ADMIN.UID, PLACE_ADMIN.TYPE)
                .values(pid, author, AUTHOR)
                .execute();
        log.info("Place {}/{} is created by {}", pid, name, author);
        return pid;
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<PlaceLink> findEditableByUid(int uid) {
        return jooq.select(PLACE.PID, PLACE.NAME, PLACE.POST_ADDRESS)
                .from(PLACE).innerJoin(PLACE_ADMIN)
                .on(PLACE.PID.eq(PLACE_ADMIN.PID))
                .where(PLACE_ADMIN.UID.eq(uid).and(PLACE_ADMIN.TYPE.in(AUTHOR)))
                .stream()
                .map(r -> PlaceLink.builder()
                        .name(r.getValue(PLACE.NAME))
                        .pid(r.getValue(PLACE.PID))
                        .address(PlaceAddress.builder()
                                .address(r.getValue(PLACE.POST_ADDRESS))
                                .build())
                        .build())
                .collect(toList());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<PlaceInfoCountTables> getPlaceById(int pid) {
        return Optional.ofNullable(
                jooq
                        .select(PLACE.PID, PLACE.NAME, PLACE.POST_ADDRESS,
                                PLACE.PHONE, PLACE.EMAIL,
                                TABLES.TABLE_ID.count().as(TBL_COUNT))
                        .from(PLACE)
                        .leftJoin(TABLES)
                        .on(PLACE.PID.eq(TABLES.PID))
                        .where(PLACE.PID.eq(pid))
                        .fetchOne())
                .map(r -> PlaceInfoCountTables.builder()
                        .name(r.getValue(PLACE.NAME))
                        .pid(r.getValue(PLACE.PID))
                        .tables(r.getValue(TBL_COUNT, TABLES.TABLE_ID.getType()))
                        .address(PlaceAddress.builder()
                                .address(r.getValue(PLACE.POST_ADDRESS))
                                .phone(r.get(PLACE.PHONE))
                                .email(r.get(PLACE.EMAIL))
                                .build())
                        .build());
    }
}
