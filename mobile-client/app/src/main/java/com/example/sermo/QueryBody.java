package com.example.sermo;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;

public class QueryBody {

    @SerializedName("tags")
    HashSet<String> tags;

    public QueryBody(HashSet<String> tags) {
        this.tags = tags;
    }
}
