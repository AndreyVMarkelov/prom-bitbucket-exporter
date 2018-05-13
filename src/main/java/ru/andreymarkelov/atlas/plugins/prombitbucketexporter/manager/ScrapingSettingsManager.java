package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.manager;

public interface ScrapingSettingsManager {
    int getDelay();
    void setDelay(int delay);
}
