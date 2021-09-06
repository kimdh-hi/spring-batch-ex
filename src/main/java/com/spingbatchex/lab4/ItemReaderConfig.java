package com.spingbatchex.lab4;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.compiler.nodes.java.ArrayLengthNode;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ItemReaderConfig {
    private final JobBuilderFactory jbf;
    private final StepBuilderFactory sbf;

    @Bean
    public Job itemReaderJob() {
        return jbf.get("itemReaderJob")
                .incrementer(new RunIdIncrementer())
                .start(customItemReaderStep())
                .build();
    }

    @Bean
    public Step customItemReaderStep() {
        return sbf.get("customItemReaderStep")
                .<Person, Person>chunk(10)
                .reader(new CustomItemReader<>(getItems(10)))
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<Person> itemWriter() {
        return items -> log.info(items.stream()
                .map(Person::getId).collect(Collectors.joining(", ")));
    }


    private List<Person> getItems(int n) {
        List<Person> items = new ArrayList<>();
        for (int i=0;i<n;i++) {
            items.add(new Person("testId"+i, "testName"+i, 10+i));
        }
        return items;
    }
}
