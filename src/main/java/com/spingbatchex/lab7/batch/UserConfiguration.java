package com.spingbatchex.lab7.batch;

import com.spingbatchex.lab7.domain.User;
import com.spingbatchex.lab7.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserConfiguration {
    private final JobBuilderFactory jbf;
    private final StepBuilderFactory sbf;
    private final UserRepository userRepository;
    private final EntityManagerFactory emf;

    @Bean
    public Job userJob() throws Exception {
        return jbf.get("userJob")
                .incrementer(new RunIdIncrementer())
                .start(saveUserStep())
                .next(userLevelUpStep())
                .listener(new LevelUpJobExecutionListener(userRepository))
                .build();
    }

    @Bean
    public Step saveUserStep() {
        return sbf.get("saveUserStep")
                .tasklet(new SaveUserTasklet(userRepository))
                .build();
    }

    @Bean
    public Step userLevelUpStep() throws Exception {
        return sbf.get("userLevelUpStep")
                .<User, User>chunk(100)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<? super User> itemWriter() {

        return users -> {
            users.forEach(user -> {
                user.levelUp();
                userRepository.save(user);
            });
        };
    }

    private ItemProcessor<? super User, ? extends User> itemProcessor() {

        return user -> {
            if (user.isValidToLevelup()) return user;
            return null;
        };
    }

    private ItemReader<? extends User> itemReader() throws Exception {

        JpaPagingItemReader<User> reader = new JpaPagingItemReaderBuilder<User>()
                .name("userItemReader")
                .queryString("select u from User u")
                .entityManagerFactory(emf)
                .pageSize(100)
                .build();
        reader.afterPropertiesSet();

        return reader;
    }



}
