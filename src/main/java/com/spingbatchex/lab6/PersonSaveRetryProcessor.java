package com.spingbatchex.lab6;

import com.spingbatchex.lab4.Person;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

public class PersonSaveRetryProcessor implements ItemProcessor<Person, Person> {

    private final RetryTemplate retryTemplate;

    public PersonSaveRetryProcessor() {
        retryTemplate = new RetryTemplateBuilder()
                .retryOn(NotFoundNameException.class)
                .maxAttempts(3)
                .build();
    }

    @Override
    public Person process(Person item) throws Exception {
        return retryTemplate.execute(
                context -> {
                    // RetryCallback
                    if (!item.getName().isEmpty()) return item;
                    throw new NotFoundNameException();
                },

                context -> {
                    // RecoveryCallback
                    item.setName("UNKNOWN");
                    return item;
                }
        );
    }
}
