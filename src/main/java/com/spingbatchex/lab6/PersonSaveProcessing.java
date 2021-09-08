package com.spingbatchex.lab6;

import com.spingbatchex.lab4.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PersonSaveProcessing {
    private final JobBuilderFactory jbf;
    private final StepBuilderFactory sbf;
    private final EntityManagerFactory emf;

    @Bean
    public Job personSaveJob() throws Exception {
        return jbf.get("personSaveJob")
                .incrementer(new RunIdIncrementer())
                .start(personSaveStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step personSaveStep(@Value("#{jobParameters[allowDup]}") String allowDup) throws Exception {
        return sbf.get("personSaveStep")
                .<Person, Person>chunk(10)
                .reader(itemReader())
                .processor(new DuplicateValidateProcessor<Person>(Person::getName, Boolean.parseBoolean(allowDup)))
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<? super Person> itemWriter() throws Exception {

        //return items -> items.forEach(item -> log.info("name is {}", item.getName()));

        JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
                .entityManagerFactory(emf)
                .build();

        ItemWriter<Person> logItemWriter = items -> log.info("item size = {}", items.size());

        CompositeItemWriter<Person> compositeItemWriter = new CompositeItemWriterBuilder<Person>()
                .delegates(jpaItemWriter, logItemWriter)
                .build();
        compositeItemWriter.afterPropertiesSet();

        return compositeItemWriter;
    }

    private ItemReader<? extends Person> itemReader() throws Exception {
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("name", "age");
        lineMapper.setLineTokenizer(lineTokenizer);

        lineMapper.setFieldSetMapper(fieldSet -> {
            return new Person(fieldSet.readString("name"), fieldSet.readInt("age"));
        });

        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
                .name("personFlatFileReader")
                .resource(new ClassPathResource("test/test.csv"))
                .encoding("UTF-8")
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .build();
        itemReader.afterPropertiesSet();

        return itemReader;
    }

}
