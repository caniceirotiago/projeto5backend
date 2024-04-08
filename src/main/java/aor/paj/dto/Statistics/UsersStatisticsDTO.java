package aor.paj.dto.Statistics;

import java.util.HashMap;
import java.time.Month;

public class UsersStatisticsDTO {
    private int totalUsers;
    private int confirmedUsers;
    private int unconfirmedUsers;
    private double averageTasksPerUser;
    private HashMap<String, Integer> usersPerMonth;

    public UsersStatisticsDTO() {}

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getConfirmedUsers() {
        return confirmedUsers;
    }

    public void setConfirmedUsers(int confirmedUsers) {
        this.confirmedUsers = confirmedUsers;
    }

    public int getUnconfirmedUsers() {
        return unconfirmedUsers;
    }

    public void setUnconfirmedUsers(int unconfirmedUsers) {
        this.unconfirmedUsers = unconfirmedUsers;
    }

    public double getAverageTasksPerUser() {
        return averageTasksPerUser;
    }

    public void setAverageTasksPerUser(double averageTasksPerUser) {
        this.averageTasksPerUser = averageTasksPerUser;
    }

    public HashMap<String, Integer> getUsersPerMonth() {
        return usersPerMonth;
    }

    public void setUsersPerMonth(HashMap<String, Integer> usersPerMonth) {
        this.usersPerMonth = usersPerMonth;
    }
}
