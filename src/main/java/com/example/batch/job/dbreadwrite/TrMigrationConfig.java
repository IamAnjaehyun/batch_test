package com.example.batch.job.dbreadwrite;

import com.example.batch.core.domain.accounts.Accounts;
import com.example.batch.core.domain.accounts.AccountsRepository;
import com.example.batch.core.domain.orders.Orders;
import com.example.batch.core.domain.orders.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.Collections;

/**
 * desc: 주문 테이블 -> 정산 테이블 데이터 이관
 * run: --spring.batch.job.name=trMigrationJob
 */
@Configuration
@RequiredArgsConstructor
public class TrMigrationConfig {
    private OrdersRepository ordersRepository;
    private AccountsRepository accountsRepository;

    @Bean
    public Job trMigrationJob(JobRepository jobRepository, @Qualifier("trMigrationStep") Step step) {
        return new JobBuilder("trMigrationJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step trMigrationStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                                @Qualifier("trOrdersReader") ItemReader trOrdersReader,
                                @Qualifier("trOrderProcessor") ItemProcessor trOrderProcessor,
                                @Qualifier("trOrdersWriter") ItemWriter trOrdersWriter) {
        return new StepBuilder("helloWorldStep", jobRepository)
                .<Orders, Accounts>chunk(5, platformTransactionManager) //5개만큼 처리 후에 commit 하겠다
                .reader(trOrdersReader)
//                .writer(new ItemWriter() {
//                    @Override
//                    public void write(Chunk chunk) throws Exception {
//                        chunk.forEach(System.out::println);
//                    }
//                })
                .processor(trOrderProcessor) //가공
                .writer(trOrdersWriter) //쓰기
                .build();
    }

//    @StepScope
//    @Bean
//    public RepositoryItemWriter<Accounts> trOrdersWriter(AccountsRepository accountsRepository) {
//        return new RepositoryItemWriterBuilder<Accounts>()
//                .repository(accountsRepository)
//                .methodName("save")
//                .build();
//    }

    //둘 다 사용 가능

    @StepScope
    @Bean
    public ItemWriter<Accounts> trOrdersWriter(AccountsRepository accountsRepository){
        return new ItemWriter<Accounts>() {
            @Override
            public void write(Chunk<? extends Accounts> chunk) throws Exception {
                chunk.forEach(item -> accountsRepository.save(item));
            }
        };
    }

    @StepScope
    @Bean
    public ItemProcessor<Orders, Accounts> trOrderProcessor() {
        return new ItemProcessor<Orders, Accounts>() {
            @Override
            public Accounts process(Orders item) throws Exception {
                return new Accounts(item); //orders 내역 받아서 accounts로 변경
            }
        };
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Orders> trOrdersReader(OrdersRepository ordersRepository) {
        return new RepositoryItemReaderBuilder<Orders>()
                .name("trOrdersReader")
                .repository(ordersRepository)
                .methodName("findAll")
                .pageSize(5) // 청크 사이즈랑 같게
                .arguments(Arrays.asList())
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }
}

