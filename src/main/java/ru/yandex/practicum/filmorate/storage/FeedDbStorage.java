package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final String SQL_GET_FEED = "SELECT * FROM FEED WHERE USER_ID = ?";

    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Event> getFeed(long id) {
        final List<Event> feed = jdbcTemplate.query(SQL_GET_FEED, FeedDbStorage::makeEvent, id);
        if (feed.size() == 0) {
            log.debug(String.format("Не найдена активность пользователя %d.", id));
            return Collections.emptyList();
        }
        return feed;
    }

    @Override
    public void addEvent(Event event) {
        final String sqlQuery = "insert into FEED(USER_ID,EVENT_TYPE,OPERATION,ENTITY_ID,TIMESTAMP) " +
                "values (?, ?, ?, ?, ?)";
        int row = jdbcTemplate.update(sqlQuery
                , event.getUserId()
                , event.getEventType()
                , event.getOperation()
                , event.getEntityId()
                , event.getTimestamp());
    }

    public static Event makeEvent(ResultSet rs, int rowNum) throws SQLException {
        return new Event(rs.getInt("EVENT_ID"),
                rs.getInt("USER_ID"),
                rs.getString("EVENT_TYPE"),
                rs.getString("OPERATION"),
                rs.getInt("ENTITY_ID"),
                rs.getLong("TIMESTAMP"));
    }
}
