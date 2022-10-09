package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.List;

@Slf4j
@Service
public class FeedServiceImpl implements FeedService {
    private final FeedStorage feedStorage;

    public FeedServiceImpl(FeedStorage feedStorage) {
        this.feedStorage = feedStorage;
    }

    @Override
    public List<Event> getFeed(long id) {
        return feedStorage.getFeed(id);
    }
}
