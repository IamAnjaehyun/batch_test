//package com.example.batch.job.joblistener;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
///**
// * desc : Hello world 출력
// * run: --spring.batch.job.name=JobListenerJob
// */
//@Configuration
//@RequiredArgsConstructor
//public class JobListenerConfig {
//    @Bean
//    public Job JobListenerJob(JobRepository jobRepository, @Qualifier("JobListenerStep") Step step) {
//        return new JobBuilder("listenerJob", jobRepository)
//                .listener(new JobLoggerListener())
//                .start(step)
//                .build();
//    }
//
//    @Bean
//    public Step JobListenerStep(JobRepository jobRepository, @Qualifier("JobListenerTasklet") Tasklet tasklet, PlatformTransactionManager platformTransactionManager) {
//        return new StepBuilder("helloWorldStep", jobRepository)
//                .tasklet(tasklet, platformTransactionManager)
//                .allowStartIfComplete(true)
//                .build();
//    }
//
//    @Bean
//    public Tasklet JobListenerTasklet() {
//        return (((contribution, chunkContext) -> {
////            System.out.println("=== JobListener is running ===");
////            return RepeatStatus.FINISHED;
//            throw new Exception("Failed!");
//        }));
//    }
//}
