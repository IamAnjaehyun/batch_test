package com.example.batch.job.jobflow;

import com.example.batch.core.domain.users.Users;
import com.example.batch.core.domain.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

/**
 * desc: on / to test
 * run: --spring.batch.job.name=flowJob
 */
@Configuration
@RequiredArgsConstructor
public class JobFlowConfig {
    @Bean
    public Job flowJob(JobRepository jobRepository,
                       @Qualifier("stepA") Step stepA,
                       @Qualifier("stepB") Step stepB,
                       @Qualifier("stepC") Step stepC

    ) {
        return new JobBuilder("flowJob", jobRepository)
                .start(stepA).on("FAILED")   // STEP A가 FAILED 인 경우
                .to(stepC)                          // StepC로 이동
                .on("*").end()               // StepC의 결과와 관계없이 Flow 종료
                .from(stepA).on("*")         // StepA로부터 FAILED 외의 모든 경우에
                .to(stepB).on("*").end()     // StepB로 이동, 결과와 관계없이 Flow 종료
                .end()                              // Job 종료
                .build();
    }

    @JobScope
    @Bean
    public Step stepA(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                      @Qualifier("itemReaderA") ItemReader itemReaderA,
                      @Qualifier("itemWriterA") ItemWriter itemWriterA
    ) {
        System.out.println("====================== stepA 시작 ======================");
        return new StepBuilder("stepA", jobRepository)
                .allowStartIfComplete(true)
                .<String, String>chunk(5, transactionManager)
                .reader(itemReaderA)
                .writer(itemWriterA)
                .build();
    }

    @JobScope
    @Bean
    public Step stepB(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                      @Qualifier("itemReaderA") ItemReader itemReaderA,
                      @Qualifier("itemWriterA") ItemWriter itemWriterA
    ) {
        System.out.println("====================== stepB 시작 ======================");
        return new StepBuilder("stepB", jobRepository)
                .allowStartIfComplete(true)
                .<String, String>chunk(5, transactionManager)
                .reader(itemReaderA)
                .writer(itemWriterA)
                .build();
    }

    @JobScope
    @Bean
    public Step stepC(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                      @Qualifier("itemReaderA") ItemReader itemReaderA,
                      @Qualifier("itemWriterA") ItemWriter itemWriterA
    ) {
        return new StepBuilder("stepC", jobRepository)
                .allowStartIfComplete(true)
                .<String, String>chunk(5, transactionManager)
                .reader(itemReaderA)
                .writer(itemWriterA)
                .build();
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Users> itemReaderA(UsersRepository usersRepository) { //userRepository 에서 findAll
        return new RepositoryItemReaderBuilder<Users>()
                .name("itemReaderA")
                .repository(usersRepository)
                .methodName("findAll")
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .pageSize(5)        // Chunk Size 와 같게
                .build();
    }

    @StepScope
    @Bean
    public ItemWriter<Users> itemWriterA() {
        return chunk -> {
            for (Users user : chunk) {
                System.out.println(user.getName());
            }
        };
    }


}
