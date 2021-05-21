package wooteco.subway.line.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wooteco.subway.line.dto.CreateLineDto;
import wooteco.subway.line.dto.DeleteSectionRequest;
import wooteco.subway.line.dto.LineRequest;
import wooteco.subway.line.dto.LineResponse;
import wooteco.subway.line.dto.LineServiceDto;
import wooteco.subway.line.dto.LineWithComposedStationsDto;
import wooteco.subway.line.dto.SectionRequest;
import wooteco.subway.line.dto.UpdateLineRequest;
import wooteco.subway.line.service.LineService;
import wooteco.subway.section.dto.CreateSectionDto;

@RestController
@RequestMapping("/lines")
public class LineController {

    private final LineService lineService;

    public LineController(final LineService lineService) {
        this.lineService = lineService;
    }

    @PostMapping
    public ResponseEntity<LineResponse> createLine(@Valid @RequestBody final LineRequest lineRequest) {
        CreateLineDto createLineDto = CreateLineDto.from(lineRequest);
        LineServiceDto createdLineServiceDto = lineService.createLine(createLineDto);
        LineResponse lineResponse = LineResponse.from(createdLineServiceDto);

        return ResponseEntity.created(URI.create("/lines/" + lineResponse.getId()))
            .body(lineResponse);
    }

    @GetMapping
    public ResponseEntity<List<LineResponse>> showLines() {
        List<LineServiceDto> lines = lineService.findAllDto();
        List<LineResponse> lineResponses = lines.stream()
            .map(LineResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(lineResponses);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<LineResponse> showLine(@PathVariable final Long id) {
        LineWithComposedStationsDto lineWithComposedStationsDto = lineService.findOne(new LineServiceDto(id));
        LineResponse lineResponse = LineResponse.from(lineWithComposedStationsDto);
        return ResponseEntity.ok(lineResponse);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Void> updateLine(
        @Valid @RequestBody final UpdateLineRequest updateLineRequest, @PathVariable final Long id) {

        LineServiceDto lineServiceDto = LineServiceDto.from(id, updateLineRequest);
        lineService.update(lineServiceDto);

        return ResponseEntity.ok()
            .build();
    }

    @PostMapping(value="/{id}/sections")
    public ResponseEntity<Void> createSection(@Valid @RequestBody final SectionRequest sectionRequest,
        @PathVariable final long id) {
        CreateSectionDto createSectionDto = CreateSectionDto.of(id, sectionRequest);
        lineService.saveSection(createSectionDto);

        return ResponseEntity.ok()
            .build();
    }

    @DeleteMapping(value="/{lineId}/sections")
    public ResponseEntity<Void> deleteStationOnSection(@PathVariable final long lineId,
        @RequestParam final long stationId) {
        DeleteSectionRequest deleteSectionRequest = new DeleteSectionRequest(lineId, stationId);
        lineService.deleteSection(deleteSectionRequest);

        return ResponseEntity.noContent()
            .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLine(@PathVariable final Long id) {
        lineService.delete(new LineServiceDto(id));

        return ResponseEntity.noContent()
            .build();
    }
}