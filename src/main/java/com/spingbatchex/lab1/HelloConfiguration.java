package com.spingbatchex.lab1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class HelloConfiguration {

    private final JobBuilderFactory jbf;
    private final StepBuilderFactory sbf;

    /**
     * Job, Step 생성
     */

    @Bean
    public Job helloJob() {
        return jbf.get("helloJob")
                .incrementer(new RunIdIncrementer()) // 항상 새로운 Job 인스턴스를 생성
                .start(helloStep())
                .build();
    }

    @Bean
    public Step helloStep() {
        return sbf.get("helloStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("hello spring-batch");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
