package com.example.sermo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Report implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("full_name")
    @Expose
    private String full_name;

    @SerializedName("age")
    @Expose
    private String age;

    @SerializedName("gender")
    @Expose
    private String gender;

    @SerializedName("consultation_summary")
    @Expose
    private String consultation_summary;

    @SerializedName("operation_summary")
    @Expose
    private String operation_summary;

    @SerializedName("review_summary")
    @Expose
    private String review_summary;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAge() {
        return age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setConsultation_summary(String consultation_summary) {
        this.consultation_summary = consultation_summary;
    }

    public String getConsultation_summary() {
        return consultation_summary;
    }

    public void setOperation_summary(String operation_summary) {
        this.operation_summary = operation_summary;
    }

    public String getOperation_summary() {
        return operation_summary;
    }

    public void setReview_summary(String review_summary) {
        this.review_summary = review_summary;
    }

    public String getReview_summary() {
        return review_summary;
    }
}
