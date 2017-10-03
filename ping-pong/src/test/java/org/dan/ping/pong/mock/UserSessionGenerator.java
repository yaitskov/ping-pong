package org.dan.ping.pong.mock;

import static java.util.stream.Collectors.toList;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.app.user.UserType;
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
        return generate(UUID.randomUUID().toString(), UserType.User);
    }

    public TestUserSession generate(String name, UserType user) {
        final UserInfo userInfo = daoEntityGenerator.genUser(name, user);
        return TestUserSession.builder()
                .uid(userInfo.getUid())
                .email(userInfo.getEmail().get())
                .session(daoEntityGenerator.genUserSession(userInfo.getUid()))
                .build();
    }

    public List<TestUserSession> generateUserSessions(int n) {
        return generateUserSessions(valueGenerator.genName(20), n);
    }

    public List<TestUserSession> generateUserSessions(String prefix, int n) {
        return generateUserSessions(prefix, IntStream.range(1, n + 1)
                .mapToObj(String::valueOf)
                .collect(toList()));
    }

    public List<TestUserSession> generateUserSessions(String prefix,
            List<String> prefixes) {
        final List<TestUserSession> result = new ArrayList<>(prefixes.size());
        final StringBuilder uids = new StringBuilder();
        for (int i = 0; i < prefixes.size(); ++i) {
            final TestUserSession session = generate(prefix + prefixes.get(i), UserType.User);
            result.add(session);
            uids.append(" ").append(prefixes.get(i))
                    .append("/").append(session.getUid());
        }
        log.info("Generated {} users with prefix [{}]: [{} ]", prefixes.size(), prefix,
                uids.toString());
        return result;
    }
}
