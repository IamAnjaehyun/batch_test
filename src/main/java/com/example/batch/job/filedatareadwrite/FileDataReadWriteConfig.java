package com.example.batch.job.filedatareadwrite;

import com.example.batch.job.filedatareadwrite.dto.Player;
import com.example.batch.job.filedatareadwrite.dto.PlayerYears;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * desc: 파일 읽고 쓰기
 * run: --spring.batch.job.name=fileDataReadWriteJob
 */
@Configuration
@RequiredArgsConstructor
public class FileDataReadWriteConfig {
    @Bean
    public Job fileDataReadWriteJob(JobRepository jobRepository, @Qualifier("fileDataReadWriteStep") Step step) {
        return new JobBuilder("fileDataReadWriteJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step fileDataReadWriteStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                                      ItemReader playerItemReader,
                                      ItemProcessor playerItemProcessor,
                                      ItemWriter playerItemWriter) {
        return new StepBuilder("fileDataReadWriteStep", jobRepository)
                .<Player, PlayerYears>chunk(5, platformTransactionManager)
                .reader(playerItemReader)
//            .writer(new ItemWriter() { //단순 출력
//                @Override
//                public void write(Chunk chunk) throws Exception {
//                    chunk.forEach(System.out::println);
//                }
//            })
                .processor(playerItemProcessor)
                .writer(playerItemWriter)
                .build();
    }

    @Bean
    public ItemProcessor<Player, PlayerYears> playerItemProcessor() {
        return new ItemProcessor<Player, PlayerYears>() {
            @Override
            public PlayerYears process(Player item) throws Exception {
                return new PlayerYears(item);
            }
        };
    }


    @StepScope
    @Bean
    public FlatFileItemReader<Player> playerItemReader() { //file에서 데이터 읽어올 수 있는 reader
        return new FlatFileItemReaderBuilder<Player>()
                .name("playerItemReader")
                .resource(new FileSystemResource("Players.csv"))
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PlayerFieldSetMapper()) //읽어온 데이터를 객체로 만들 수 있도록
                .linesToSkip(1) //첫 번째줄 제목이라 스킵
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemWriter<PlayerYears> playerItemWriter() {
        BeanWrapperFieldExtractor<PlayerYears> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"ID", "lastName", "position", "yearsExperience"});
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<PlayerYears> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FileSystemResource outputResource = new FileSystemResource("players_output.txt");

        return new FlatFileItemWriterBuilder<PlayerYears>()
                .name("playerItem")
                .resource(outputResource)
                .lineAggregator(lineAggregator)
                .build();
    }

}
