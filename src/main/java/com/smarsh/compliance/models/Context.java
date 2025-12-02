package com.smarsh.compliance.models;

import lombok.Data;

import java.util.List;

@Data
public class Context {
    private String team;
    private String channel;
    private List<String> recipients;
}