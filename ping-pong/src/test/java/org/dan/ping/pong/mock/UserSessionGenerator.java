package org.dan.ping.pong.mock;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class UserSessionGenerator {
    public static final String USER_SESSION = "userSession";

    @Inject
    private DaoEntityGenerator daoEntityGenerator;

    @Bean(name = USER_SESSION)
    @Scope(SCOPE_PROTOTYPE)
    public TestUserSession generate() {
        final int userId = daoEntityGenerator.genUser();
        return TestUserSession.builder()
                .uid(userId)
                .session(daoEntityGenerator.genUserSession(userId))
                .build();
    }

    public List<TestUserSession> generateUserSessions(int n) {
        List<TestUserSession> result = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            result.add(generate());
        }
        return result;
    }
}
