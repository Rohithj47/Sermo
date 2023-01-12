package com.example.sermo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReportsCollection {
    @SerializedName("reports")
    private List<Report> reports;

    public List<Report> getReports() {
        return reports;
    }
}
