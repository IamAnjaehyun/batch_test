package com.example.batch.job.chunktasklet;

import com.example.batch.core.domain.users.Users;
import com.example.batch.core.domain.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * desc: Users 테이블 조회하여 휴면유저 판별
 * run: --spring.batch.job.name=usersJob
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChunkConfig {
    private static final int chunkSize = 5;
    private JobLauncher jobLauncher;


    @Autowired
    public ChunkConfig(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

//    Job 실행 Scheduler
//    @Scheduled(cron = "1 * * * * *") // 매 분 1초마다 실행
//    public void runUsersStepJob() throws Exception {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addString("Job", "test" + LocalDateTime.now())
//                .toJobParameters();
//        jobLauncher.run(chunkJob(null, null), jobParameters);
//    }

    //Step 을 실행 시키는 Job
    @Bean
    public Job chunkJob(JobRepository jobRepository, @Qualifier("chunkStep") Step usersStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .start(usersStep) // 단일 Step Job
                .build();
    }

    @Bean
    public Step chunkStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                          @Qualifier("chunkReader") ItemReader chunkReader,
                          @Qualifier("chunkProcessor") ItemProcessor chunkProcessor,
                          @Qualifier("chunkWriter") ItemWriter chunkWriter) {
        TaskletStep chunkStep = new StepBuilder("chunkStep", jobRepository)
                .allowStartIfComplete(true)
                .<Users, Users>chunk(chunkSize, platformTransactionManager) //chunk 5 -> 5개만큼 처리 후에 commit
                .reader(chunkReader)        //데이터 읽기    -> ItemReader
                .processor(chunkProcessor)  //데이터 가공    -> ItemProcessor
                .writer(chunkWriter)        //데이터 쓰기    -> ItemWriter
                .build();
        return chunkStep;
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Users> chunkReader(UsersRepository usersRepository) {
        return new RepositoryItemReaderBuilder<Users>()
                .name("chunkReader")
                .repository(usersRepository)
                .methodName("findAll")
                .pageSize(chunkSize)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }


    @StepScope
    @Bean
    public ItemProcessor<Users, String> chunkProcessor() {
        return item -> item.getName() + " ### processor";
    }

    @StepScope
    @Bean
    public ItemWriter<String> chunkWriter() {
        return items -> {
            items.forEach(item -> {
                System.out.println("### writer : " + item);
            });
        };
    }

}

