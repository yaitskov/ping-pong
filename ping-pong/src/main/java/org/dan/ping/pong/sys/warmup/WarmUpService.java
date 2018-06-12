package org.dan.ping.pong.sys.warmup;

import static org.dan.ping.pong.sys.warmup.WarmUpCtx.WARM_UP_CACHE;

import com.google.common.cache.Cache;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.util.time.Clocker;

import java.time.Instant;

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

    public int warmUp(Uid user, WarmUpRequest request) {
        final String action = warmUpCache.getIfPresent(request.getAction());
        if (action == null) {
            warmUpCache.put(request.getAction(), request.getAction());
            return warmUpDao.warmUp(user, request, clocker.get());
        } else {
            return 0;
        }
    }

    public int logDuration(int warmUpId, Instant clientStarted) {
        return warmUpDao.logDuration(warmUpId, clocker.get(), clientStarted);
    }
}
