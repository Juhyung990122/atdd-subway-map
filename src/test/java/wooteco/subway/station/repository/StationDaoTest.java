package wooteco.subway.station.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import wooteco.subway.station.domain.Station;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
class StationDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private StationDao stationDao;

    @BeforeEach
    void setUp() {
        stationDao = new StationDao(jdbcTemplate);

        jdbcTemplate.update("ALTER TABLE STATION ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM STATION");
    }

    @Test
    void save() {
        stationDao.save(new Station("잠실역"));

        Station station = stationDao.findById(1L);

        assertThat(station).isEqualTo(new Station(1L, "잠실역"));
    }

    @Test
    void findAll() {
        stationDao.save(new Station("잠실역1"));
        stationDao.save(new Station("잠실역2"));
        stationDao.save(new Station("잠실역3"));

        List<Station> stations = stationDao.findAll();

        assertThat(stations).isEqualTo(Arrays.asList(
                new Station(1L, "잠실역1"),
                new Station(2L, "잠실역2"),
                new Station(3L, "잠실역3")
        ));
    }

    @Test
    void findById() {
        stationDao.save(new Station("잠실역"));

        Station station = stationDao.findById(1L);

        assertThat(station).isEqualTo(new Station(1L, "잠실역"));
    }

    @Test
    void delete() {
        stationDao.save(new Station("dummy"));

        stationDao.delete(1L);

        assertThatThrownBy(() -> stationDao.findById(1L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

}