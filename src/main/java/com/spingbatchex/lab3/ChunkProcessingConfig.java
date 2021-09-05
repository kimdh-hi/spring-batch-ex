package com.spingbatchex.lab3;

import antlr.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .next(chunkBasedStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step chunkBasedStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {
        return sbf.get("chunkStep")
                // <INPUT_TYPE, OUTPUT_TYPE>
                // chunk(num) : itemWriter 의 OUTPUT_TYPE 리스트의 크기
                .<String,String>chunk(chunkSize.isEmpty() ? 10 : Integer.parseInt(chunkSize))
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
        return items -> log.info("(chunk) chunk size = {}", items.size());
    }

    @Bean
    public Step taskBasedStep() {
        return sbf.get("taskStep")
                .tasklet(tasklet())
                .build();
    }

    /**
     * tasklet 기반 step에서 chunk 구현
     */
    private Tasklet tasklet() {

        List<String> testData = getTestData();

        return (contribution, chunkContext) -> {
            StepExecution stepExecution = contribution.getStepExecution();
            JobParameters jobParameters = stepExecution.getJobParameters();

            int chunkSize = Integer.parseInt(jobParameters.getString("chunkSize", "0"));
            //int chunkSize = 10;
            int fromIdx = stepExecution.getReadCount();
            int toIdx = fromIdx + chunkSize;

            if (fromIdx >= testData.size()) return RepeatStatus.FINISHED;

            log.info("(task) chunk size = {}", testData.subList(fromIdx, toIdx).size());

            stepExecution.setReadCount(toIdx);

            return RepeatStatus.CONTINUABLE;
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
