package wooteco.subway.controller.dto.request;

import javax.validation.constraints.NotEmpty;

public class StationRequest {

    @NotEmpty
    private String name;

    public StationRequest() {
    }

    public StationRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}