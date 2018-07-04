package org.dan.ping.pong.app.suggestion;

import static java.time.Duration.between;
import static org.dan.ping.pong.app.suggestion.PageAdr.ofSize;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@Slf4j
public class SuggestionService {
    @Inject
    private SuggestionDao suggestionDao;

    @Inject
    private Clocker clocker;

    @Inject
    private PersistentPatternGenerator persistentPatternGenerator;

    @Inject
    private SearchPatternGenerator searchPatternGenerator;

    @Value("${suggestion.max.lookup.duration}")
    private Duration maxLookupDuration;

    public List<UserLink> findCandidates(
            Uid requester, SuggestReq req) {
        final Instant started = clocker.get();
        final List<PatternInterpretation> parts = searchPatternGenerator
                .interpretPattern(req.getPattern());
        final List<UserLink> result = new ArrayList<>();
        final List<Uid> acceptedUids = new ArrayList<>();
        final int stopLimit = req.getPage().total();
        for (PatternInterpretation part : parts) {
            suggestionDao.findUsers(
                    requester, part, acceptedUids,
                    ofSize(stopLimit - acceptedUids.size()))
                    .stream()
                    .peek(u -> acceptedUids.add(u.getUid()))
                    .forEach(result::add);
            final Duration duration = between(started, clocker.get());
            if (duration.compareTo(maxLookupDuration) > 0) {
                log.info("Break suggestion [{}] lookup for {}",
                        req.getPattern(), requester);
                break;
            }
            if (acceptedUids.size() >= stopLimit) {
                break;
            }
        }

        return result;
    }

    @Transactional(TRANSACTION_MANAGER)
    public void createSuggestion(Uid requester, String name,
            Uid targetUid, DbUpdater batch) {
        final List<PatternInterpretation> patterns = persistentPatternGenerator
                .interpretPattern(name);
        final Instant now = clocker.get();
        for (PatternInterpretation pattern : patterns) {
            suggestionDao.createIndexRow(pattern.getIdxType(), pattern.getPattern(),
                    requester, targetUid, now, batch);
        }
    }
}
