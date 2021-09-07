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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ItemReaderConfig {
    private final JobBuilderFactory jbf;
    private final StepBuilderFactory sbf;
    private final DataSource dataSource;
    private final EntityManagerFactory emf;

    @Bean
    public Job itemReaderJob() throws Exception {
        return jbf.get("itemReaderJob")
                .incrementer(new RunIdIncrementer())
                .start(customItemReaderStep())
                .next(csvFileReaderStep())
                .next(jdbcCursorItemReaderStep())
                .next(jpaCursorItemReaderStep())
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

    @Bean
    public Step csvFileReaderStep() throws Exception {
        return sbf.get("csvFileReaderStep")
                .<Person, Person>chunk(10)
                .reader(this.csvFileReader())
                .writer(this.itemWriter())
                .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep() throws Exception {
        return sbf.get("jdbcCursorItemStep")
                .<Person, Person>chunk(10)
                .reader(this.jdbcCursorItemReader())
                .writer(this.itemWriter())
                .build();
    }

    @Bean
    public Step jpaCursorItemReaderStep() throws Exception {
        return sbf.get("JpaCursorItemReaderStep")
                .<Person,Person>chunk(10)
                .reader(this.jpaCursorItemReader())
                .writer(this.itemWriter())
                .build();
    }

    /**
     * JpaCursorItemReader
     */
    private JpaCursorItemReader<Person> jpaCursorItemReader() throws Exception {
        JpaCursorItemReader<Person> itemReader = new JpaCursorItemReaderBuilder<Person>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(emf)
                .queryString("select p from Person p")
                .build();
        itemReader.afterPropertiesSet();
        return itemReader;
    }

    /**
     * JdbcCursorItemReader
     */
    private JdbcCursorItemReader<Person> jdbcCursorItemReader() throws Exception {
        JdbcCursorItemReader<Person> itemReader = new JdbcCursorItemReaderBuilder<Person>()
                .name("jdbcCursorItemReader")
                .dataSource(dataSource)
                .sql("select id, name, age from person")
                .rowMapper((rs, rowNum) ->
                    new Person(
                            Long.valueOf(rs.getInt(1)),
                            rs.getString(2),
                            rs.getInt(3))
                )
                .build();
        itemReader.afterPropertiesSet();
        return itemReader;
    }

    /**
     * FlatFileItemReader - csv 파일 read
     */
    private FlatFileItemReader<Person> csvFileReader() throws Exception {
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer(); // ' , ' 로 구분
        lineTokenizer.setNames("id","name","age");
        lineMapper.setLineTokenizer(lineTokenizer);

        lineMapper.setFieldSetMapper(fieldSet -> {
            int id = fieldSet.readInt("id");
            String name = fieldSet.readString("name");
            int age = fieldSet.readInt("age");

            return new Person(Long.valueOf(id), name, age);
        });

        FlatFileItemReader<Person> csvFileReader = new FlatFileItemReaderBuilder<Person>()
                .name("csvFileReader")
                .encoding("UTF-8")
                .resource(new ClassPathResource("test/test.csv"))
                .linesToSkip(1) // 레이블 row 스킵
                .lineMapper(lineMapper)
                .build();
        // ItemReader에 필요한 필수 속성값이 정상적으로 설정되었는지 검증
        csvFileReader.afterPropertiesSet();

        return csvFileReader;
    }

    private ItemWriter<Person> itemWriter() {
        return items -> log.info(items.stream()
                .map(Person::getName).collect(Collectors.joining(", ")));
    }


    private List<Person> getItems(int n) {
        List<Person> items = new ArrayList<>();
        for (int i=0;i<n;i++) {
            items.add(new Person(Long.valueOf(i), "testName"+i, 10+i));
        }
        return items;
    }
}
