package com.example.batch.job.dbreadwrite;

import com.example.batch.core.domain.orders.Orders;
import com.example.batch.core.domain.users.Users;
import com.example.batch.core.domain.users.UsersRepository;
import com.example.batch.core.domain.users.type.Status;
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
import org.springframework.batch.item.Chunk;
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
import java.util.Arrays;
import java.util.Collections;

/**
 * desc: 주문 테이블 -> 정산 테이블 데이터 이관
 * run: --spring.batch.job.name=trMigrationJob
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UsersConfig {
    @Autowired
    private JobLauncher jobLauncher;

    @Scheduled(cron = "1 * * * * *") // 매 분 1초에 실행
    public void runUsersStepJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobName", "휴면유저 정리 Job")
                .addString("startTime", LocalDateTime.now().toString()) // 현재 시간을 매개변수로 추가
                .toJobParameters();
        jobLauncher.run(usersJob(null, null), jobParameters);
    }


    @Bean
    public Job usersJob(JobRepository jobRepository, @Qualifier("usersStep") Step usersStep) {
        return new JobBuilder("usersJob", jobRepository)
                .start(usersStep)
                .build();
    }

    @Bean
    public Step usersStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                          @Qualifier("usersReader") ItemReader usersReader,
                          @Qualifier("userProcessor") ItemProcessor usersProcessor,
                          @Qualifier("usersWriter") ItemWriter usersWriter) {
        return new StepBuilder("usersStep", jobRepository)
                .<Users, Users>chunk(5, platformTransactionManager) //5개만큼 처리 후에 commit
                .reader(usersReader) //데이터 불러오기
                .processor(usersProcessor) //가공
                .writer(usersWriter) //쓰기
                .build();
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Orders> usersReader(UsersRepository usersRepository) {
        return new RepositoryItemReaderBuilder<Orders>()
                .name("usersReader")
                .repository(usersRepository)
                .methodName("findAll")
                .pageSize(5) // 청크 사이즈랑 같게
                .arguments(Arrays.asList())
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @StepScope
    @Bean
    public ItemProcessor<Users, Users> userProcessor() {
        return new ItemProcessor<Users, Users>() {
            @Override
            public Users process(Users users) throws Exception {
                LocalDate currentDate = LocalDate.now();
                // modifiedAt 날짜
                LocalDate modifiedAtDate = users.getModifiedAt();

                // modifiedAt 날짜가 현재 날짜로부터 1주일 이전인지 확인
                long weeksBetween = ChronoUnit.WEEKS.between(modifiedAtDate, currentDate);

                // modifiedAt 날짜가 현재 날짜로부터 1년 이전인지 확인
                long yearsBetween = ChronoUnit.YEARS.between(modifiedAtDate, currentDate);
                if (users.getStatus() == Status.NEW_USER) {
                    if (yearsBetween >= 1 && weeksBetween >= 1) {
                        users.setStatus(Status.OLD_USER);
                        log.info(users.getName() + "님이 마지막으로 접속한지 " + yearsBetween + "년, (" + weeksBetween + ") 주 경과되어 " + users.getStatus() + "로 회원 상태가 변경되었습니다.");
                    } else if (weeksBetween >= 1) {
                        users.setStatus(Status.USER);
                        log.info(users.getName() + "님이 마지막으로 접속한지 " + yearsBetween + "년, (" + weeksBetween + ") 주 경과되어 " + users.getStatus() + "로 회원 상태가 변경되었습니다.");
                    }
                }
                return users;
            }
        };
    }

    @StepScope
    @Bean
    public ItemWriter<Users> usersWriter(UsersRepository usersRepository) {
        return new ItemWriter<Users>() {
            @Override
            public void write(Chunk<? extends Users> chunk) throws Exception {
                chunk.forEach(usersRepository::save);
            }
        };
    }
}

