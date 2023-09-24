package webserver.example;

import webserver.annotations.JsonField;

public class RunCommandResponse {


    @JsonField
    private final int status;
    @JsonField
    private final boolean refused;

    public RunCommandResponse(int status, boolean refused) {
        this.status = status;
        this.refused = refused;
    }

    public int getStatus() {
        return status;
    }

    public boolean isRefused() {
        return refused;
    }
}
