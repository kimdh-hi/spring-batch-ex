package com.spingbatchex.lab6;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class DuplicateValidateProcessor<T> implements ItemProcessor<T, T> {
    // key 중복 체크를 위한 Map
    private final Map<String, Object> map = new ConcurrentHashMap<>();
    // 중복체크 하고자 하는 필드의 getter를 넘겨주면 해당 값을 key로 쓰도록 함 (확장성)
    private final Function<T, String> keyExtractor;
    // 중복허용여부
    private final boolean allowDup;

    public DuplicateValidateProcessor(Function<T, String> keyExtractor, boolean allowDup) {
        this.keyExtractor = keyExtractor;
        this.allowDup = allowDup;
    }

    @Override
    public T process(T item) throws Exception {
        if (allowDup) return item; // 중복 허용시 조건검사 없이 리턴

        String key = keyExtractor.apply(item); // 중복체크 대상 키값 추출

        if (map.containsKey(key)) return null; // 중복인 경우 null 리턴하여 writer 대상에서 제외
        map.put(key,key); // 중복이 아닌 경우 이후 중복처리를 위해 map에 삽입

        return item;
    }
}
