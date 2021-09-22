package com.spingbatchex.lab7;

import com.spingbatchex.lab6.TestConfiguration;
import com.spingbatchex.lab7.batch.UserConfiguration;
import com.spingbatchex.lab7.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UserConfiguration.class, TestConfiguration.class})
public class UserConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void test() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        int size = userRepository.findAllByUpdatedAt(LocalDate.now()).size();

        assertThat(
                jobExecution.getStepExecutions()
                        .stream()
                        .filter(job -> job.getStepName().equals("userLevelUpStep"))
                        .mapToInt(StepExecution::getWriteCount)
        ).isEqualTo(size).isEqualTo(300);

        assertThat(userRepository.count())
                .isEqualTo(400);
    }
}
