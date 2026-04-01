package com.sourav.hacknovation;

public class EvaluatorTeamModel {
    private String teamKey;
        public String projectId;
        public String teamName;
        public String theme;
        public String leader;


    public String getTeamKey() {
        return teamKey;
    }

    public void setTeamKey(String teamKey) {
        this.teamKey = teamKey;
    }

        public EvaluatorTeamModel() {
            // Required for Firebase
        }


}
