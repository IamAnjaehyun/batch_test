package com.example.batch.job.userstatus;

import com.example.batch.BatchApplication;
import com.example.batch.core.domain.users.Users;
import com.example.batch.core.domain.users.UsersRepository;
import com.example.batch.core.domain.users.type.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

/**
 * desc: Users 테이블 조회하여 휴면유저 판별
 * run: --spring.batch.job.name=usersJob
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UsersConfig {
    private static final int chunkSize = 5;
    private JobLauncher jobLauncher;

    private static final Logger logger= LoggerFactory.getLogger(BatchApplication.class);


    @Autowired
    public UsersConfig(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

//    Job 실행 Scheduler
//    @Scheduled(cron = "0 0 3 * * *") // 매일 오전 3시에 실행
    @Scheduled(cron = "1 * * * * *") // 매 분 1초마다 실행
    public void runUsersStepJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("Job 이름", "휴면유저 판별")
                .addString("작업 일시", String.valueOf(LocalDateTime.now())) // 현재 시간을 Parameter 로 추가
                .toJobParameters();
        jobLauncher.run(usersJob(null, null), jobParameters);
    }

    //Step 을 실행 시키는 Job
    @Bean
    public Job usersJob(JobRepository jobRepository, @Qualifier("usersStep") Step usersStep) {
        return new JobBuilder("usersJob", jobRepository)
                .start(usersStep) // 단일 Step Job
                .build();
    }

    @JobScope
    @Bean
    public Step usersStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                          @Qualifier("usersReader") ItemReader<Users> usersReader,
                          @Qualifier("userProcessor") ItemProcessor<Users, Users> usersProcessor,
                          @Qualifier("usersWriter") ItemWriter<Users> usersWriter) {
        return new StepBuilder("usersStep", jobRepository)
                .allowStartIfComplete(true)                          //재시작 가능하도록
                .<Users, Users>chunk(5, platformTransactionManager)    //chunk 5 & 한 번에 5개씩 transaction
                .reader(usersReader)                                 //데이터 읽기    -> ItemReader
                .processor(usersProcessor)                           //데이터 가공    -> ItemProcessor
                .writer(usersWriter)                                 //데이터 쓰기    -> ItemWriter
                .build();
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Users> usersReader(UsersRepository usersRepository) { //userRepository 에서 findAll
        return new RepositoryItemReaderBuilder<Users>()
                .name("usersReader")
                .repository(usersRepository)
                .methodName("findAll")
//                .methodName("findNewUserAndUser") //다른 jpa method 사용 가능
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .pageSize(5)                        // Chunk Size 와 같게 & 한 번에 조회할 Item의 양
                .build();
    }

    @StepScope
    @Bean
    public ItemProcessor<Users, Users> userProcessor() {
        return users -> {
            //회원 가입 7일 경과 -> 일반 회원
            if (users.getStatus() == Status.NEW_USER) {
                //회원 가입 일자 판별
                long daysBetween = ChronoUnit.DAYS.between(users.getCreatedAt(), LocalDate.now());

                if (daysBetween >= 7) { // 7일(1주) 이상
                    users.setStatus(Status.USER);
                    logger.info(users.getName() + " 님이 계정을 생성한 지 7 일이 경과 되어 " + Status.USER + "로 회원 상태를 변경하였습니다.");
                }
            }

            //USER 상태일 때
            if (users.getStatus() == Status.USER) {
                // modifiedAt(마지막 로그인) 날짜 판별
                long daysBetween = ChronoUnit.DAYS.between(users.getLastLogin(), LocalDate.now());

                if (daysBetween >= 365) { // 1년(365일) 이상
                    users.setStatus(Status.OLD_USER);
                    logger.info(users.getName() + " 님의 마지막 접속 기록이 365 일 (이상) 경과 되어 " + Status.OLD_USER + "로 회원 상태를 변경하였습니다.");
                }
            }
            return users;
        };

    }

    @StepScope
    @Bean
    public ItemWriter<Users> usersWriter(UsersRepository usersRepository) {
        return chunk -> chunk.forEach(usersRepository::save);
    }
}

