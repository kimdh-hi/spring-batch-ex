package com.spingbatchex.lab4;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.List;

public class CustomItemReader<T> implements ItemReader<T> {

    private List<T> items;

    public CustomItemReader(List<T> items) {
        this.items = new ArrayList<>(items);
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        if (!items.isEmpty()) {
            return items.remove(0);
        }

        // null을 리턴한다는 것은 chunk 반복을 종료한다는 의미
        return null;
    }
}
