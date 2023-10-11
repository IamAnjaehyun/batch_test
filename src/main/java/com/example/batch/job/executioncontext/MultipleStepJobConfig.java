package com.example.batch.job.executioncontext;

import com.example.batch.job.filedatareadwrite.FileDataReadWriteConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

/**
 * desc: step별 관리
 * run: --spring.batch.job.name=multipleStepJob
 */

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultipleStepJobConfig {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private FileDataReadWriteConfig fileDataReadWriteConfig;

//    @Scheduled(cron = "1 * * * * *") // 매 분 1초에 실행
//    public void runMultipleStepJob() throws Exception {
//        JobParameters jobParameters1 = new JobParametersBuilder()
//                .addString("jobName", "첫 번째 작업 & 여러 스텝이 있는 작업")
//                .addString("startTime", LocalDateTime.now().toString()) // 현재 시간을 매개변수로 추가
//                .toJobParameters();
//        jobLauncher.run(multipleStepJob(null, null, null, null), jobParameters1);
//    }

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
    public Step multipleStep3(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                              @Qualifier("JobListenerMultipleTasklet3") Tasklet tasklet) {
        return new StepBuilder("multipleStep3", jobRepository)
                .tasklet(tasklet, platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Tasklet JobListenerMultipleTasklet1() {
        return (((contribution, chunkContext) -> {
            log.warn("==============                        JobListenerMultipleTasklet1 시작                                        =======");

            ExecutionContext executionContext = chunkContext
                    .getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext();

            executionContext.put("tasklet1", "!!Tasklet 1 에서 출발!!");

            log.warn("==============        tasklet1 의 Value 값인  <<" + executionContext.get("tasklet1") + ">> 를 ExecutionContext 에 담기            =======");
            log.warn("==============                        JobListenerMultipleTasklet1 종료                                        =======");

            return RepeatStatus.FINISHED;
        }));
    }

    @Bean
    public Tasklet JobListenerMultipleTasklet2() {
        return (((contribution, chunkContext) -> {
            log.warn("==============                        JobListenerMultipleTasklet2 시작                                        =======");

            ExecutionContext executionContext = chunkContext
                    .getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext();

            log.warn("==============        tasklet1 에서 가져온 데이터 tasklet1의 value = " + executionContext.get("tasklet1") + " ===> 2에서 호출        =======");
            executionContext.put("tasklet1", "!!Tasklet 2 에서 변환!!");
            log.warn("==============        tasklet1 에서 가져온 데이터 tasklet1의 value = " + executionContext.get("tasklet1") + " ===> 2에서 변환        =======");
            log.warn("==============                        JobListenerMultipleTasklet2 종료                                        =======");

            return RepeatStatus.FINISHED;
        }));
    }

    @Bean
    public Tasklet JobListenerMultipleTasklet3() {
        return (((contribution, chunkContext) -> {
            log.warn("==============                        JobListenerMultipleTasklet3 시작                                        =======");
            ExecutionContext executionContext = chunkContext
                    .getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext();

            log.warn("==============        tasklet1 에서 가져온 데이터 tasklet1의 value = " + executionContext.get("tasklet1") + " ===> 3에서도 호출 가능  =======");
            log.warn("==============                        JobListenerMultipleTasklet3 종료                                        =======");

            return RepeatStatus.FINISHED;
        }));
    }

}
