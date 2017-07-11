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
import org.dan.ping.pong.app.user.UserRegRequest;
import org.dan.ping.pong.sys.sadmin.SysAdminDao;
import org.dan.ping.pong.util.time.Clocker;
import org.jooq.DSLContext;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.inject.Inject;

public class DaoEntityGenerator {
    public static final String SYS_ADMIN_TEST_PASSWORD = "1";
    @Inject
    private DSLContext jooq;

    @Inject
    private SysAdminDao sysAdminDao;

    public int genSysAdmin(String login) {
        return sysAdminDao.create(login, SYS_ADMIN_TEST_PASSWORD, "salt");
    }

    public int genSysAdmin() {
        return genSysAdmin(genStr());
    }

    @Inject
    private UserDao userDao;

    public int genAdmin(int said) {
        final int uid = genUser();
        userDao.promoteToAdmins(said, uid);
        return uid;
    }

    public int genUser() {
        return userDao.register(UserRegRequest.builder()
                .name(genStr())
                .email(Optional.of(genStr() + "@email.com"))
                .phone(Optional.of(genPhone()))
                .build());
    }

    public String genUserSession() {
        return genUserSession(genUser());
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

    public int genPlace(int admin) {
        return placeDao.createAndGrant(admin, genStr(),
                PlaceAddress.builder()
                        .address(genStr())
                        .phone(Optional.of(genPhone()))
                        .build());
    }

    @Inject
    private TableDao tableDao;

    public int genPlace(int admin, int tables) {
        final int placeId = genPlace(admin);
        tableDao.createTables(placeId, tables);
        return placeId;
    }

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private Clocker clocker;

    public int genTournament(int adminId, int placeId, TournamentProps props) {
        final int tid = tournamentDao.create(adminId, CreateTournament.builder()
                .opensAt(props.getOpensAt()
                        .orElseGet(() -> clocker.get().plus(1, ChronoUnit.DAYS)))
                .placeId(placeId)
                .name(genTournamentName())
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

    public int genCategory(int tid) {
        return categoryDao.create(NewCategory.builder()
                .name(Generators.genCategoryName())
                .tid(tid)
                .build());
    }
}
