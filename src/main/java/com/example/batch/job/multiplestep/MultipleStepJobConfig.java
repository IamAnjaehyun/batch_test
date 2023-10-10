package com.example.batch.job.multiplestep;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * desc: step별 관리
 * run: --spring.batch.job.name=multipleStepJob
 */

@Configuration
@RequiredArgsConstructor
public class MultipleStepJobConfig {
    @Bean
    public Job multipleStepJob(JobRepository jobRepository, @Qualifier("multipleStep1") Step multipleStep1,
                               @Qualifier("multipleStep2") Step multipleStep2, @Qualifier("multipleStep3") Step multipleStep3) {
        return new JobBuilder("multipleStepJob", jobRepository)
                .start(multipleStep1)
                .next(multipleStep2)
                .next(multipleStep3)
                .build();
    }

    @Bean
    public Step multipleStep1(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                              @Qualifier("JobListenerMultipleTasklet1") Tasklet tasklet) {
        return new StepBuilder("multipleStep1", jobRepository)
                .tasklet(tasklet, platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step multipleStep2(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, @Qualifier("JobListenerMultipleTasklet2") Tasklet tasklet) {
        return new StepBuilder("multipleStep2", jobRepository)
                .tasklet(tasklet, platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step multipleStep3(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, @Qualifier("JobListenerMultipleTasklet3") Tasklet tasklet) {
        return new StepBuilder("multipleStep3", jobRepository)
                .tasklet(tasklet, platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Tasklet JobListenerMultipleTasklet1() {
        return (((contribution, chunkContext) -> {
            System.out.println("=== JobListenerMultipleTasklet1 시작 ===");
            return RepeatStatus.FINISHED;
        }));
    }

    @Bean
    public Tasklet JobListenerMultipleTasklet2() {
        return (((contribution, chunkContext) -> {
            System.out.println("=== JobListenerMultipleTasklet1 종료 ===");
            System.out.println("=== JobListenerMultipleTasklet2 시작 ===");
            return RepeatStatus.FINISHED;
        }));
    }

    @Bean
    public Tasklet JobListenerMultipleTasklet3() {
        return (((contribution, chunkContext) -> {
            System.out.println("=== JobListenerMultipleTasklet2 종료 ===");
            System.out.println("=== 모든 JobListenerMultipleTasklet 종료 ===");
            return RepeatStatus.FINISHED;
        }));
    }

}
