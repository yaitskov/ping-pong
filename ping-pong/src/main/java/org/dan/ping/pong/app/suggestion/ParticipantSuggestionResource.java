package org.dan.ping.pong.app.suggestion;

import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.user.UserLink;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

public class ParticipantSuggestionResource {
    public static final String PARTICIPANT_SUGGESTION = "/participant/suggestion";

    @Inject
    private SuggestionService suggestionService;

    @Inject
    private AuthService authService;

    @GET
    @Path(PARTICIPANT_SUGGESTION)
    public List<UserLink> findCandidates(
            @HeaderParam(SESSION) String session,
            SuggestReq req) {
        return suggestionService.findCandidates(
                authService.userInfoBySession(session)
                        .getUid(),
                req);
    }
}
