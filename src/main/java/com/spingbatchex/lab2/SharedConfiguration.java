package com.spingbatchex.lab2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class SharedConfiguration {

    private final JobBuilderFactory jbf;
    private final StepBuilderFactory sbf;

    /**
     * Job 내에서 Job의 ExecutionContext를 통해 Step 간 데이터 공유가 가능한지 테스트
     *
     * Job 내에서 Step의 ExecutionContext를 통해 Step 간 데이터 공유가 불가능한 것을 테스트
     *
     */

    @Bean
    public Job sharedJob() {
        return jbf.get("sharedJob")
                .incrementer(new RunIdIncrementer())
                .start(sharedStep1())
                .next(sharedStep2())
                .build();
    }

    @Bean
    public Step sharedStep1() {
        return sbf.get("sharedStep1")
                .tasklet(((contribution, chunkContext) -> {
                    StepExecution stepExecution = contribution.getStepExecution();
                    ExecutionContext stepExecutionExecutionContext = stepExecution.getExecutionContext();
                    stepExecutionExecutionContext.putString("step-test-key", "step-test-value");

                    JobExecution jobExecution = stepExecution.getJobExecution();
                    JobInstance jobInstance = jobExecution.getJobInstance();
                    ExecutionContext jobExecutionExecutionContext = jobExecution.getExecutionContext();
                    jobExecutionExecutionContext.putString("job-test-key", "job-test-value");
                    JobParameters jobParameters = jobExecution.getJobParameters();

                    log.info("*** jobName={}, stepName={}, parameters={}",
                            jobInstance.getJobName(),
                            stepExecution.getStepName(),
                            jobParameters.getLong("run.id"));

                    return RepeatStatus.FINISHED;
                })).build();
    }

    @Bean
    public Step sharedStep2() {
        return sbf.get("sharedStep2")
                .tasklet((contribution, chunkContext) -> {
                    StepExecution stepExecution = contribution.getStepExecution();
                    String stepTest
                            = stepExecution.getExecutionContext().getString("step-test-key", "step-empty");

                    JobExecution jobExecution = stepExecution.getJobExecution();
                    String jobTest
                            = jobExecution.getExecutionContext().getString("job-test-key", "job-empty");

                    log.info("*** step = {}, job = {}", stepTest, jobTest);

                    return RepeatStatus.FINISHED;
                }).build();
    }
}
