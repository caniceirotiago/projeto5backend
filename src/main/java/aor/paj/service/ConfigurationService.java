package aor.paj.service;

import aor.paj.bean.ConfigurationBean;
import aor.paj.bean.PermissionBean;
import aor.paj.dao.ConfigurationDao;
import aor.paj.dto.ConfigurationDto;
import aor.paj.entity.ConfigurationEntity;
import aor.paj.service.status.Function;
import filters.RequiresPermission;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/config")
public class ConfigurationService {

    @EJB
    private ConfigurationDao configurationDao;
    @EJB
    private ConfigurationBean configurationBean;

    /**
     * Endpoint to update a configuration value.
     * Only authorized users should be able to access this method.
     */
    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.EDIT_CONFIGURATION_INFO)
    public Response updateConfiguration(ConfigurationDto configurationDto) {
        boolean exists = configurationDao.configExists(configurationDto.getConfigKey());
        if (exists) {
            boolean success = configurationDao.updateConfigValue(configurationDto.getConfigKey(), configurationDto.getConfigValue());
            if (success) {
                return Response.ok().entity("{\"message\":\"Configuration updated successfully.\"}").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"message\":\"Error updating configuration.\"}").build();
            }
        } else {
            ConfigurationEntity newConfig = new ConfigurationEntity(configurationDto.getConfigKey(), configurationDto.getConfigValue(), "");
            configurationDao.persist(newConfig);
            return Response.status(Response.Status.CREATED).entity("{\"message\":\"Configuration added successfully.\"}").build();
        }
    }

    /**
     * Endpoint to retrieve a configuration value.
     */
    @GET
    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission(Function.GET_CONFIGURATION_INFO)
    public Response getConfiguration(@PathParam("key") String key) {
        String config = configurationBean.findConfigValueByKey(key);
        if (config != null) {
            return Response.ok(new ConfigurationDto(key, config)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\":\"Configuration not found.\"}").build();
        }
    }
}
