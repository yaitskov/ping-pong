package org.dan.ping.pong.mock;

import static org.dan.ping.pong.app.tournament.TournamentState.Hidden;
import static org.dan.ping.pong.mock.Generators.genPhone;
import static org.dan.ping.pong.mock.Generators.genStr;
import static org.dan.ping.pong.mock.Generators.genTournamentName;

import org.dan.ping.pong.app.auth.AuthDao;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.category.NewCategory;
import org.dan.ping.pong.app.city.CityDao;
import org.dan.ping.pong.app.city.CityLink;
import org.dan.ping.pong.app.city.NewCity;
import org.dan.ping.pong.app.country.CountryDao;
import org.dan.ping.pong.app.country.NewCountry;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.place.PlaceAddress;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.tournament.CreateTournament;
import org.dan.ping.pong.app.tournament.ForTestTournamentDao;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.TournamentDaoMySql;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.app.user.UserRegRequest;
import org.dan.ping.pong.app.user.UserType;
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

    public DaoEntityGenerator() {
    }

    public Uid genAdmin(int said) {
        final Uid uid = genUser().getUid();
        userDao.promoteToAdmins(said, uid);
        return uid;
    }

    public UserInfo genUser() {
        return genUser(valueGenerator.genName(), UserType.User);
    }

    public UserInfo genUser(String name, UserType user) {
        final UserRegRequest request = UserRegRequest.builder()
                .name(name)
                .email(Optional.of(name.replaceAll("[ \t]", "_") + "@gmail.com"))
                .phone(Optional.of("+48 799 33 8448"))
                .userType(user)
                .build();
        return UserInfo.builder()
                .uid(userDao.register(request))
                .email(request.getEmail())
                .phone(request.getPhone())
                .name(request.getName())
                .userType(request.getUserType())
                .build();
    }

    @Inject
    private AuthDao authDao;

    public String genUserSession(Uid uid) {
        final String token = "test-session-for-uid-" + uid.getId() + genStr(11);
        authDao.saveSession(uid, token, "test-device-info-for-uid-" + uid.getId());
        return token;
    }

    @Inject
    private CountryDao countryDao;

    public int genCountry(String name, Uid admin) {
        return countryDao.create(admin, NewCountry.builder().name(name).build());
    }

    @Inject
    private CityDao cityDao;

    public int genCity(String name, Uid admin) {
        return genCity(genCountry(name, admin), name, admin);
    }

    public int genCity(int countryId, String name, Uid admin) {
        return cityDao.create(admin, NewCity.builder().countryId(countryId).name(name).build());
    }

    @Inject
    private PlaceDao placeDao;

    public Pid genPlace(int cityId, String name, Uid admin) {
        return placeDao.createAndGrant(admin, name,
                PlaceAddress.builder()
                        .city(CityLink.builder().id(cityId).build())
                        .address(valueGenerator.genName())
                        .phone(Optional.of(genPhone()))
                        .build());
    }

    @Inject
    private TableDao tableDao;

    public Pid genPlace(int cityId, String name, Uid admin, int tables) {
        final Pid placeId = genPlace(cityId, name, admin);
        tableDao.createTables(placeId, tables);
        return placeId;
    }

    public Pid genPlace(Uid admin, int tables) {
        return genPlace(genCity(genCountry(genStr(), admin), genStr(), admin),
                genStr(), admin, tables);
    }

    @Inject
    private TournamentDaoMySql tournamentDao;

    @Inject
    private ForTestTournamentDao forTestTournamentDao;

    @Inject
    private Clocker clocker;

    public Tid genTournament(Uid adminId, Pid placeId, TournamentProps props) {
        return genTournament(genTournamentName(), adminId, placeId, props);
    }

    public Tid genTournament(String name, Uid adminId, Pid placeId, TournamentProps props) {
        final Tid tid = tournamentDao.create(adminId, CreateTournament.builder()
                .opensAt(props.getOpensAt()
                        .orElseGet(() -> clocker.get().plus(1, ChronoUnit.DAYS)))
                .placeId(placeId)
                .name(name)
                .state(Hidden)
                .previousTid(Optional.empty())
                .rules(props.getRules())
                .ticketPrice(Optional.empty())
                .sport(props.getSport())
                .build());
        forTestTournamentDao.setState(tid, props.getState());
        return tid;
    }

    @Inject
    private CategoryDao categoryDao;

    public int genCategory(String name, Tid tid) {
        return categoryDao.create(NewCategory.builder()
                .name(name)
                .tid(tid)
                .build());
    }
}
