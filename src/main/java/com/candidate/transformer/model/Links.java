package com.candidate.transformer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates online profiles, portfolios, and links associated with a candidate.
 */
public class Links {

    private String linkedin;
    private String github;
    private String portfolio;
    private Map<String, String> other = new HashMap<>();

    public Links() {
    }

    public Links(String linkedin, String github, String portfolio, Map<String, String> other) {
        this.linkedin = linkedin;
        this.github = github;
        this.portfolio = portfolio;
        this.other = other != null ? other : new HashMap<>();
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public Map<String, String> getOther() {
        return other;
    }

    public void setOther(Map<String, String> other) {
        this.other = other;
    }

    public void addOtherLink(String key, String url) {
        if (this.other == null) {
            this.other = new HashMap<>();
        }
        this.other.put(key, url);
    }

    @Override
    public String toString() {
        return "Links{" +
                "linkedin='" + linkedin + '\'' +
                ", github='" + github + '\'' +
                ", portfolio='" + portfolio + '\'' +
                ", other=" + other +
                '}';
    }
}
