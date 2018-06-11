package org.dan.ping.pong.sys.warmup;

import static org.dan.ping.pong.sys.warmup.WarmUpCtx.WARM_UP_CACHE;

import com.google.common.cache.Cache;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.util.time.Clocker;

import javax.inject.Inject;
import javax.inject.Named;

public class WarmUpService {
    @Inject
    private WarmUpDao warmUpDao;
    @Inject
    private Clocker clocker;
    @Inject
    @Named(WARM_UP_CACHE)
    private Cache<String, String> warmUpCache;

    public int warmUp(UserInfo user, WarmUpRequest request) {
        final String action = warmUpCache.getIfPresent(request.getAction());
        if (action == null) {
            return warmUpDao.warmUp(user.getUid(), request, clocker.get());
        } else {
            return 0;
        }
    }

    public void logDuration(int warmUpId) {
        warmUpDao.logDuration(warmUpId, clocker.get());
    }
}
