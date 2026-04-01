package com.sourav.hacknovation;

import androidx.annotation.Keep;
import java.util.Map;

@Keep
public class TeamModel {

    public String teamName;
    public String theme;
    public String leader;
    public String key;

    public String projectId;
    public int membersCount;
    public boolean verified;
    public int rank;
    public int totalScore;

    public String leaderEmail;
    public String leaderPhone;
    public Map<String, Object> members;
    public Map<String, Object> Evaluations;


    public TeamModel() {}

    @Keep
    public int getTotal() {

        if (totalScore > 0) {
            return totalScore;
        }

        if (Evaluations == null || Evaluations.isEmpty()) {
            return 0;
        }

        try {
            for (Object roundObj : Evaluations.values()) {

                if (!(roundObj instanceof Map)) continue;
                Map<?, ?> roundData = (Map<?, ?>) roundObj;

                for (Object evalObj : roundData.values()) {

                    if (!(evalObj instanceof Map)) continue;
                    Map<?, ?> evalData = (Map<?, ?>) evalObj;

                    Object totalObj = evalData.get("total");
                    if (totalObj instanceof Number) {
                        return ((Number) totalObj).intValue();
                    }
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }
}
