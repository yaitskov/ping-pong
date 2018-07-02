package org.dan.ping.pong.mock;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.mock.simulator.Player.p23;
import static org.dan.ping.pong.mock.simulator.Player.p33;
import static org.dan.ping.pong.mock.simulator.Player.p35;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.app.user.UserType;
import org.dan.ping.pong.mock.simulator.Player;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.inject.Inject;

@Slf4j
public class UserSessionGenerator {
    public static final String USER_SESSION = "userSession";

    @Inject
    private DaoEntityGenerator daoEntityGenerator;

    @Inject
    private ValueGenerator valueGenerator;

    @Bean(name = USER_SESSION)
    @Scope(SCOPE_PROTOTYPE)
    public TestUserSession generate() {
        return generate(p35, UUID.randomUUID().toString(), UserType.User);
    }

    public TestUserSession generate(Player player, String name, UserType user) {
        final UserInfo userInfo = genUser(name, user);
        return TestUserSession.builder()
                .uid(userInfo.getUid())
                .player(player)
                .email(userInfo.getEmail().get())
                .name(name)
                .session(daoEntityGenerator.genUserSession(userInfo.getUid()))
                .build();
    }

    public UserInfo genUser(String name, UserType user) {
        return daoEntityGenerator.genUser(name, user);
    }

    public List<TestUserSession> generateUserSessions(int n) {
        return generateUserSessions(valueGenerator.genName(20), n);
    }

    public List<TestUserSession> generateUserSessions(String prefix, int n) {
        return generateUserSessions(prefix, IntStream.range(1, n + 1)
                .mapToObj(String::valueOf)
                .collect(toList()));
    }

    public List<TestUserSession> generateUserSessions(String prefix, List<String> prefixes) {
        final List<TestUserSession> result = new ArrayList<>(prefixes.size());
        final StringBuilder uids = new StringBuilder();
        for (int i = 0; i < prefixes.size(); ++i) {
            final TestUserSession session = generate(p33, prefix + prefixes.get(i), UserType.User);
            result.add(session);
            uids.append(" ").append(prefixes.get(i));
        }
        log.info("Generated {} users with prefix [{}]: [{} ]", prefixes.size(), prefix,
                uids.toString());
        return result;
    }
}
