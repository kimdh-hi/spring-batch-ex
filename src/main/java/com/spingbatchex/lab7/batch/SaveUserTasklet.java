package com.spingbatchex.lab7.batch;

import com.spingbatchex.lab7.domain.User;
import com.spingbatchex.lab7.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class SaveUserTasklet implements Tasklet {

    private final UserRepository userRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<User> users = createUsers();
        Collections.shuffle(users);

        userRepository.saveAll(users);

        return RepeatStatus.FINISHED;
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        // 처음 User가 생성될 때 Level은 NORMAL임으로 totalAmount 1,000일 때 등급변화 없음
        for (int i=0;i<100;i++) {
            users.add(User.builder()
                    .username("testUser1")
                    .totalAmount(1_000)
                    .build());
        }

        // 200,000일 때 NORMAL에서 SILVER로 등급상승
        for (int i=100;i<200;i++) {
            users.add(User.builder()
                    .username("testUser2")
                    .totalAmount(200_000)
                    .build());
        }

        // totalAmount 300,000일 때 NORMAL에서 VIP로 등급상승
        for (int i=200;i<300;i++) {
            users.add(User.builder()
                    .username("testUser3")
                    .totalAmount(300_000)
                    .build());
        }

        // totalAmount 200,000일 때 NORMAL에서 VIP로 등급상승
        for (int i=300;i<400;i++) {
            users.add(User.builder()
                    .username("testUser4")
                    .totalAmount(500_000)
                    .build());
        }


        return users;
    }


}
