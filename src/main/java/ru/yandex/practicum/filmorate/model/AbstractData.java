package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@RequiredArgsConstructor
public abstract class AbstractData implements Comparable<AbstractData> {
    protected int id;

    @Override
    public int compareTo(AbstractData o) {
        return this.id - o.id;
    }
}
