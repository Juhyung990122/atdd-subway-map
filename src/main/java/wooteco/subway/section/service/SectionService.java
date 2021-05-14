package wooteco.subway.section.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.line.domain.LineRoute;
import wooteco.subway.section.dao.SectionDao;
import wooteco.subway.section.domain.Section;
import wooteco.subway.section.dto.SectionRequest;
import wooteco.subway.section.exception.SectionIllegalArgumentException;
import wooteco.subway.station.dao.StationDao;
import wooteco.subway.station.exception.StationIllegalArgumentException;

@Service
public class SectionService {

    public static final int DELETE_STATION_IN_LINE_LIMIT = 2;
    public static final int INSERT_SECTION_IN_LINE_LIMIT = 1;
    public static final int INSERT_SECTION_IN_LINE_DISTANCE_GAP_LIMIT = 0;

    private final StationDao stationDao;
    private final SectionDao sectionDao;

    public SectionService(SectionDao sectionDao, StationDao stationDao) {
        this.stationDao = stationDao;
        this.sectionDao = sectionDao;
    }

    @Transactional
    public void insertSectionInLine(Long lineId, SectionRequest sectionRequest) {
        Section section = sectionRequest.toSection(lineId);

        List<Section> sectionsByLineId = sectionDao.findAllByLineId(section.getLineId());
        LineRoute lineRoute = new LineRoute(sectionsByLineId);
        Set<Long> stationIds = lineRoute.getStationIds();

        validateStationsInSectionIsExist(sectionRequest);
        validateIfSectionContainsOnlyOneStationInLine(stationIds, section);

        if (lineRoute.isInsertSectionInEitherEndsOfLine(section)) {
            sectionDao.save(section);
            return;
        }
        insertSectionInMiddleOfLine(section, lineRoute);
        sectionDao.save(section);
    }

    private void validateStationsInSectionIsExist(SectionRequest sectionRequest) {
        stationDao.findById(sectionRequest.getDownStationId()).orElseThrow( () -> new StationIllegalArgumentException("해당 지하철 역이 존재하지 않습니다."));
        stationDao.findById(sectionRequest.getUpStationId()).orElseThrow( () -> new StationIllegalArgumentException("해당 지하철 역이 존재하지 않습니다."));
    }

    private void validateIfSectionContainsOnlyOneStationInLine(Set<Long> stationIds, Section section) {
        long count = stationIds.stream()
            .filter(stationId -> section.equalWithUpStationId(stationId) || section.equalWithDownStationId(stationId))
            .count();

        if (count != INSERT_SECTION_IN_LINE_LIMIT) {
            throw new SectionIllegalArgumentException("구간의 역 중에서 한개의 역만은 노선에 존재하여야 합니다.");
        }
    }

    private void insertSectionInMiddleOfLine(Section section, LineRoute lineRoute) {
        Section updateSection = lineRoute.getSectionNeedToBeUpdatedForInsert(section);
        validateSectionDistanceGap(updateSection);
        sectionDao.update(updateSection);
    }

    private void validateSectionDistanceGap(Section section) {
        if (section.getDistance() <= INSERT_SECTION_IN_LINE_DISTANCE_GAP_LIMIT) {
            throw new SectionIllegalArgumentException("입력하신 구간의 거리가 잘못되었습니다.");
        }
    }

    @Transactional
    public void delete(Long lineId, Long stationId) {
        List<Section> sectionsByLineId = sectionDao.findAllByLineId(lineId);
        LineRoute lineRoute = new LineRoute(sectionsByLineId);

        if (lineRoute.getStationsSize() == DELETE_STATION_IN_LINE_LIMIT) {
            throw new SectionIllegalArgumentException("종점은 삭제 할 수 없습니다.");
        }

        Optional<Section> upSectionOpt = lineRoute
            .getSectionFromUpToDownStationMapByStationId(stationId);
        Optional<Section> downSectionOpt = lineRoute
            .getSectionFromDownToUpStationMapByStationId(stationId);

        if (isDeleteStationInMiddleOfLine(upSectionOpt, downSectionOpt)) {
            insertMergeSectionBeforeDelete(lineId, upSectionOpt, downSectionOpt);
        }

        deleteEachSectionContainsDeleteStation(upSectionOpt, downSectionOpt);
    }

    private boolean isDeleteStationInMiddleOfLine(Optional<Section> upSectionOpt, Optional<Section> downSectionOpt) {
        return upSectionOpt.isPresent() && downSectionOpt.isPresent();
    }

    private void insertMergeSectionBeforeDelete(Long lineId, Optional<Section> upSectionOpt, Optional<Section> downSectionOpt) {
        Section upSection = upSectionOpt.get();
        Section downSection = downSectionOpt.get();
        Section mergeSection = Section.of(lineId, downSection.getUpStationId(), upSection.getDownStationId(), upSection.getDistance() + downSection.getDistance());
        sectionDao.save(mergeSection);
    }

    private void deleteEachSectionContainsDeleteStation(Optional<Section> upSectionOpt, Optional<Section> downSectionOpt) {
        upSectionOpt.ifPresent(section -> sectionDao.delete(section.getId()));
        downSectionOpt.ifPresent(section -> sectionDao.delete(section.getId()));
    }
}