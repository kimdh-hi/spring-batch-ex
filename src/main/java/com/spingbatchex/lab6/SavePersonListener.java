package com.spingbatchex.lab6;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;


@Slf4j
public class SavePersonListener {

    public static class SavePersonStepExecutionListener {

        @BeforeStep
        public void beforeStep(StepExecution stepExecution) {
            log.info("beforeStep called");
        }

        @AfterStep
        public void afterStep(StepExecution stepExecution) {
            log.info("afterStep : write_count = {}", stepExecution.getWriteCount());
        }
    }
}
