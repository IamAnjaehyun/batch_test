//package com.example.batch.job.HelloWorld;
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
// * run: --spring.batch.job.name=myJob
// */
//@Configuration
//@RequiredArgsConstructor
//public class HelloWorldConfig {
//    @Bean
//    public Job helloJobWorld(JobRepository jobRepository, @Qualifier("helloWorldStep") Step step){
//        return new JobBuilder("myJob", jobRepository)
//                .start(step)
//                .build();
//    }
//
//    @Bean
//    public Step helloWorldStep(JobRepository jobRepository,@Qualifier("helloWorldTasklet") Tasklet tasklet, PlatformTransactionManager platformTransactionManager) {
//        return new StepBuilder("helloWorldStep", jobRepository)
//                .tasklet(tasklet, platformTransactionManager).build();
//    }
//
//    @Bean
//    public Tasklet helloWorldTasklet() {
//        return (((contribution, chunkContext) -> {
//            System.out.println("Hello World Spring Batch");
//            return RepeatStatus.FINISHED;
//        }));
//    }
//}
