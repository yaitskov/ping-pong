package org.dan.ping.pong.mock;

import static java.util.stream.Collectors.toList;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.user.UserInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        return generate(UUID.randomUUID().toString());
    }

    public TestUserSession generate(String name) {
        final UserInfo userInfo = daoEntityGenerator.genUser(name);
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
        final List<TestUserSession> result = new ArrayList<>(n);
        final StringBuilder uids = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            final TestUserSession session = generate(String.format("%s p%03d", prefix, i));
            result.add(session);
            uids.append(" p").append(1 + i).append("/").append(session.getUid());
        }
        log.info("Generated {} users with prefix [{}]: [{} ]", n, prefix,
                uids.toString());
        return result;
    }
}
