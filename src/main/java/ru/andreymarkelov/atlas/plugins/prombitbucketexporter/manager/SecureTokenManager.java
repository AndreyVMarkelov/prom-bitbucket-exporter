package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

public interface SecureTokenManager {
    String getToken();
    void setToken(String token);
}
