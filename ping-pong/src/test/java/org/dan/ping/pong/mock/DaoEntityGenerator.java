package org.dan.ping.pong.mock;

import static org.dan.ping.pong.mock.Generators.genPhone;
import static org.dan.ping.pong.mock.Generators.genStr;
import static org.dan.ping.pong.mock.Generators.genTournamentName;

import org.dan.ping.pong.app.auth.AuthDao;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.NewCategory;
import org.dan.ping.pong.app.place.PlaceAddress;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.tournament.CreateTournament;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.app.user.UserRegRequest;
import org.dan.ping.pong.util.time.Clocker;
import org.jooq.DSLContext;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.inject.Inject;

public class DaoEntityGenerator {
    @Inject
    private DSLContext jooq;

    @Inject
    private UserDao userDao;

    @Inject
    private ValueGenerator valueGenerator;

    public int genAdmin(int said) {
        final int uid = genUser().getUid();
        userDao.promoteToAdmins(said, uid);
        return uid;
    }

    public UserInfo genUser() {
        return genUser(valueGenerator.genName());
    }

    public UserInfo genUser(String name) {
        final UserRegRequest request = UserRegRequest.builder()
                .name(name)
                .email(Optional.of(name.replaceAll("[ \t]", "_") + "@gmail.com"))
                .phone(Optional.of("+48 799 33 8448"))
                .build();
        return UserInfo.builder()
                .uid(userDao.register(request))
                .email(request.getEmail())
                .phone(request.getPhone())
                .name(request.getName())
                .build();
    }

    @Inject
    private AuthDao authDao;

    public String genUserSession(int uid) {
        final String token = "test-session-for-uid-" + uid + genStr(11);
        authDao.saveSession(uid, token, "test-device-info-for-uid-" + uid);
        return token;
    }

    @Inject
    private PlaceDao placeDao;

    public int genPlace(String name, int admin) {
        return placeDao.createAndGrant(admin, name,
                PlaceAddress.builder()
                        .address(valueGenerator.genName())
                        .phone(Optional.of(genPhone()))
                        .build());
    }

    @Inject
    private TableDao tableDao;

    public int genPlace(String name, int admin, int tables) {
        final int placeId = genPlace(name, admin);
        tableDao.createTables(placeId, tables);
        return placeId;
    }

    public int genPlace(int admin, int tables) {
        return genPlace(genStr(), admin, tables);
    }

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private Clocker clocker;

    public int genTournament(int adminId, int placeId, TournamentProps props) {
        return genTournament(genTournamentName(), adminId, placeId, props);
    }

    public int genTournament(String name, int adminId, int placeId, TournamentProps props) {
        final int tid = tournamentDao.create(adminId, CreateTournament.builder()
                .opensAt(props.getOpensAt()
                        .orElseGet(() -> clocker.get().plus(1, ChronoUnit.DAYS)))
                .placeId(placeId)
                .name(name)
                .previousTid(Optional.empty())
                .maxGroupSize(props.getMaxGroupSize())
                .quitsFromGroup(props.getQuitsFromGroup())
                .ticketPrice(Optional.empty())
                .thirdPlaceMatch(0)
                .build());
        tournamentDao.setState(tid, props.getState());
        return tid;
    }

    @Inject
    private CategoryDao categoryDao;

    public int genCategory(String name, int tid) {
        return categoryDao.create(NewCategory.builder()
                .name(name)
                .tid(tid)
                .build());
    }
}
