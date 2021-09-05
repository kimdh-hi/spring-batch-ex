package com.spingbatchex.lab3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ChunkProcessingConfig {

    private final JobBuilderFactory jbf;
    private final StepBuilderFactory sbf;

    @Bean
    public Job chunkProcessingJob() {
        return jbf.get("chunkJob")
                .incrementer(new RunIdIncrementer())
                .start(taskBasedStep())
                .next(chunkBasedStep())
                .build();
    }

    @Bean
    public Step chunkBasedStep() {
        return sbf.get("chunkStep")
                .<String,String>chunk(10)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }
    // 배치 대상 데이터 get
    private ListItemReader itemReader(){
        return new ListItemReader<>(getTestData());
    }
    // 배치 대상 데이터 가공 및 필터링
    private ItemProcessor<String,String> itemProcessor() {
        return item -> item + " + batch processing";
    }
    // 배치 대상 데이터 처리
    private ItemWriter<String> itemWriter() {
        return items -> log.info("chunk size = {}", items.size());
    }

    @Bean
    public Step taskBasedStep() {
        return sbf.get("taskStep")
                .tasklet(tasklet())
                .build();
    }

    private Tasklet tasklet() {
        return (contribution, chunkContext) ->  {
            List<String> testData = getTestData();
            log.info("test data size = {}", testData.size());

            return RepeatStatus.FINISHED;
        };
    }

    private List<String> getTestData() {
        List<String> list = new ArrayList<>();
        for(int i=0;i<100;i++) {
            list.add(i + " test");
        }
        return list;
    }

}
