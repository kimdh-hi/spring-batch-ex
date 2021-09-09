package com.spingbatchex.lab6;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PersonSaveConfig.class, TestConfiguration.class})
public class PersonSaveConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    @DisplayName("1. PersonSaveProcessing 중복허용 테스트")
    public void 중복허용() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("allowDup", "true")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        Assertions.assertThat(
            jobExecution.getStepExecutions()
                .stream()
                .mapToInt(StepExecution::getWriteCount)
                .sum()
        ).isEqualTo(100);
    }
}
