package org.dan.ping.pong.mock;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.dan.ping.pong.app.user.UserInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class UserSessionGenerator {
    public static final String USER_SESSION = "userSession";

    @Inject
    private DaoEntityGenerator daoEntityGenerator;

    @Inject
    private ValueGenerator valueGenerator;

    @Bean(name = USER_SESSION)
    @Scope(SCOPE_PROTOTYPE)
    public TestUserSession generate(String name) {
        final UserInfo userInfo = daoEntityGenerator.genUser(name);
        return TestUserSession.builder()
                .uid(userInfo.getUid())
                .email(userInfo.getEmail().get())
                .session(daoEntityGenerator.genUserSession(userInfo.getUid()))
                .build();
    }

    public List<TestUserSession> generateUserSessions(int n) {
        return generateUserSessions(valueGenerator.genName(), n);
    }

    public List<TestUserSession> generateUserSessions(String prefix, int n) {
        List<TestUserSession> result = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            result.add(generate(String.format("%s p%03d", prefix, n)));
        }
        return result;
    }
}
