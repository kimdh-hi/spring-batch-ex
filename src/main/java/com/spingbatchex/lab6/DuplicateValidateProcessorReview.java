package com.spingbatchex.lab6;

import com.spingbatchex.lab4.Person;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DuplicateValidateProcessorReview<T> implements ItemProcessor<T,T> {

    private final Map<String, Object> map = new ConcurrentHashMap<>();
    private final boolean allowDup;
    private final Function<T, String> keyExchanger;

    public DuplicateValidateProcessorReview(boolean allowDup, Function<T, String> keyExchanger) {
        this.allowDup = allowDup;
        this.keyExchanger = keyExchanger;
    }

    @Override
    public T process(T item) throws Exception {

        if (allowDup) {
            return item;
        }
        // T -> String
        String key = keyExchanger.apply(item);

        if (map.containsKey(key)) {
            return null;
        }

        map.put(key, key);
        return item;
    }
}
