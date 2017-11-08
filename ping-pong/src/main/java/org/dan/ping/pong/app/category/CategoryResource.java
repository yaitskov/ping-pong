package org.dan.ping.pong.app.category;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.match.MatchResource.TID_JP;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;
import org.dan.ping.pong.app.user.UserInfo;
import org.jooq.exception.DataAccessException;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class CategoryResource {
    private static final String CATEGORY = "/category/";
    public static final String CATEGORY_MEMBERS = CATEGORY + "find/members/";
    private static final String CID_JP = "{cid}";
    private static final String CID = "cid";

    @Inject
    private CategoryDao categoryDao;

    @GET
    @Path(CATEGORY + "find/by/tid/{tid}")
    public List<CategoryLink> findByTid(@PathParam("tid") Tid tid) {
        return categoryDao.listCategoriesByTid(tid);
    }

    @Inject
    private TournamentAccessor tournamentAccessor;

    @Inject
    private CategoryService categoryService;

    @GET
    @Path(CATEGORY_MEMBERS + TID_JP + "/" + CID_JP)
    public void findMembers(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            @PathParam(TID) Tid tid,
            @PathParam(CID) int cid) {
        tournamentAccessor.read(tid, response,
                (tournament) -> categoryService.categoryInfo(tournament, cid,
                        authService.userInfoBySessionQuite(session)
                                .map(UserInfo::getUid)));
    }

    @Inject
    private AuthService authService;

    @POST
    @Path(CATEGORY + "create")
    @Consumes(APPLICATION_JSON)
    public int create(@HeaderParam(SESSION) String session, NewCategory newCategory) {
        final UserInfo user = authService.userInfoBySession(session);
        // check perms
        log.info("Add category to tid {}", newCategory.getTid());
        return categoryDao.create(newCategory);
    }

    @POST
    @Path(CATEGORY + "delete/{cid}")
    @Consumes(APPLICATION_JSON)
    public void delete(@HeaderParam(SESSION) String session,
            @PathParam("cid") int cid) {
        final UserInfo user = authService.userInfoBySession(session);
        // check perms
        try {
            categoryDao.delete(cid);
        } catch (DataAccessException e) {
            throw badRequest("Category with enlisted participants cannot deleted", e);
        }
    }
}
