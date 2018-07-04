package org.dan.ping.pong.app.suggestion;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.user.UserLink;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


@Path("/")
@Produces(APPLICATION_JSON)
public class ParticipantSuggestionResource {
    public static final String PARTICIPANT_SUGGESTION = "/participant/suggestion";

    @Inject
    private SuggestionService suggestionService;

    @Inject
    private AuthService authService;

    @POST
    @Path(PARTICIPANT_SUGGESTION)
    public List<UserLink> findCandidates(
            @HeaderParam(SESSION) String session,
            @Valid @NotNull SuggestReq req) {
        return suggestionService.findCandidates(
                authService.userInfoBySession(session)
                        .getUid(),
                req);
    }
}
