package aor.paj.service;

import aor.paj.bean.StatisticsBean;
import aor.paj.dto.Statistics.*;

import aor.paj.exception.UserConfirmationException;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/statistics")
public class StatisticsService {

    @EJB
    private StatisticsBean statisticsBean;

    @GET
    @Path("/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public DashboardDTO getDashboardData() {
        return statisticsBean.createDashboardDto();
    }
    @GET
    @Path("/individualstats/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public IndividualUserStatisticsDto getIndividualStats(@PathParam("username") String username) throws UserConfirmationException {
        return statisticsBean.createIndividualUserStatisticsDTO(username);
    }
}
