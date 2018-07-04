package org.dan.ping.pong.app.suggestion;

import org.springframework.context.annotation.Import;

@Import({SuggestionService.class, ParticipantSuggestionResource.class,
        PersistentPatternGenerator.class,
        SearchPatternGenerator.class,
        CleanUpSuggestionsJob.CleanUpSuggestionsJobDetail.class,
        SuggestionDao.class})
public class SuggestionCtx {
}
