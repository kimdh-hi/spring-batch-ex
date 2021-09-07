package com.spingbatchex.lab5;

import com.spingbatchex.lab4.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ItemWriteConfig {
    private final JobBuilderFactory jbf;
    private final StepBuilderFactory sbf;
    private final DataSource dataSource;
    private final EntityManagerFactory emf;

    @Bean
    public Job ItemWriteJob() throws Exception {
        return jbf.get("ItemWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(itemWriteStep())
                //.next(jdbcBatchItemWriteStep())
                .next(jpaItemWriterStep())
                .build();
    }

    @Bean
    public Step itemWriteStep() throws Exception {
        return sbf.get("itemWriteStep")
                .<Person,Person>chunk(10)
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Step jdbcBatchItemWriteStep() {
        return sbf.get("jdbcBatchItemWriteStep")
                .<Person,Person>chunk(10)
                .reader(itemReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    @Bean
    public Step jpaItemWriterStep() throws Exception {
        return sbf.get("jpaItemWriterStep")
                .<Person,Person>chunk(10)
                .reader(itemReader())
                .writer(jpaItemWriter())
                .build();
    }

    /**
     * JpaItemWriter
     */
    private JpaItemWriter<Person> jpaItemWriter() throws Exception {
        JpaItemWriter<Person> writer = new JpaItemWriterBuilder<Person>()
                .entityManagerFactory(emf)
                .usePersist(true) // merge가 동작하지 않도록 (merge로 인해 2번의 쿼릭 발생하지 않도록)
                .build();
        writer.afterPropertiesSet();
        return writer;
    }

    /**
     * JdbcBatchItemWriter
     * insert into person(name, age) values ("a",1),("b",2),("c",3),("d",4) .... ;
     */
    private JdbcBatchItemWriter<Person> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriterBuilder<Person>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into person(name, age) values(:name, :age)")
                .build();
        writer.afterPropertiesSet();

        return writer;
    }

    /**
     *  FlatFileItemWriter - csv 파일에 쓰기
     *  BeanWrapperFieldExtractor
     *  DelimitedLineAggregator
     *  FlatFileItemWriter
     */
    private ItemWriter<Person> itemWriter() throws Exception {
        BeanWrapperFieldExtractor<Person> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"id", "name", "age"}); // 필드이름 설정

        DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(extractor);

        FlatFileItemWriter<Person> writer = new FlatFileItemWriterBuilder<Person>()
                .name("flatFileItemWriterBuilder")
                .encoding("UTF-8")
                .lineAggregator(lineAggregator)
                .resource(new FileSystemResource("output/test2.csv"))
                .headerCallback(w -> w.write("id,이름,나이")) // 헤더필드 설정
                .footerCallback(w -> w.write("=============\n")) // 푸터필드 설정
                .append(true)
                .build();
        writer.afterPropertiesSet();

        return writer;
    }

    private ItemReader<Person> itemReader() {
        return new ListItemReader<>(getItems());
    }

    private List<Person> getItems() {
        List<Person> list = new ArrayList<>();
        for (int i=0; i<10;i++) {
            list.add(new Person("name" + i, 15));
        }
        return list;
    }
}
