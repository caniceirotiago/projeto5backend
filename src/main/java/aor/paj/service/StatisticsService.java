package aor.paj.service;

import aor.paj.bean.StatisticsBean;
import aor.paj.dto.Statistics.CategoryStatisticsDTO;
import aor.paj.dto.Statistics.DashboardDTO;
import aor.paj.dto.Statistics.TasksStatisticsDTO;
import aor.paj.dto.Statistics.UsersStatisticsDTO;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/statistics")
public class StatisticsService {

    @EJB
    private StatisticsBean statisticsBean;

    /**
     * Endpoint para estatísticas dos utilizadores.
     */
    @GET
    @Path("/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboardData() {
        UsersStatisticsDTO usersDTO = statisticsBean.createUserStatisticsDTO();
        TasksStatisticsDTO tasksDTO = statisticsBean.createTasksStatisticsDTO();
        CategoryStatisticsDTO categoryDTO = statisticsBean.createCategoryStatisticsDTO();
        DashboardDTO dashboardDTO = new DashboardDTO(usersDTO, tasksDTO, categoryDTO);
        return Response.ok(dashboardDTO).build();
    }
}
