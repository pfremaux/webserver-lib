package webserver.example.videoedition.model;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class ExtractFrameRequest {
    @JsonField
    private final String videoPath;
    @JsonField
    private final Double timeSeconds;

    public ExtractFrameRequest(
            @JsonParameter(name = "videoPath")  String videoPath,
            @JsonParameter(name = "timeSeconds") Double timeSeconds) {
        this.videoPath = videoPath;
        this.timeSeconds = timeSeconds;
    }

    public Double getTimeSeconds() {
        return timeSeconds;
    }

    public String getVideoPath() {
        return videoPath;
    }
}
