//package com.example.batch.job.validatedparam;
//
//import com.example.batch.job.validatedparam.validator.FileParamValidator;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.StepContribution;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.core.job.CompositeJobParametersValidator;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.scope.context.ChunkContext;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import java.util.List;
//
///**
// * desc : 파일 이름 파라미터 전달 그리고 검증
// * run: --spring.batch.job.name=validatedParamJob -fileName=test.csv
// */
//@Configuration
//@RequiredArgsConstructor
//public class ValidatedParamJobConfig {
//
//    @Bean
//    public Job validatedParamJob(JobRepository jobRepository, @Qualifier("validatedParamStep") Step validatedParamStep){
//        return new JobBuilder("validatedParamJob", jobRepository)
//                .validator(multipleValidator()) //여러개 등록 위해 아래와 같은 방법 사용
//                .start(validatedParamStep)
//                .build();
//    }
//
//    private CompositeJobParametersValidator multipleValidator(){
//        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
//        validator.setValidators(List.of(new FileParamValidator()));
//        return validator;
//    }
//
//    @Bean
//    public Step validatedParamStep(JobRepository jobRepository, @Qualifier("validatedParamTasklet") Tasklet validatedParamTasklet, PlatformTransactionManager platformTransactionManager) {
//        return new StepBuilder("validatedParamStep", jobRepository)
//                .tasklet(validatedParamTasklet, platformTransactionManager)
//                .allowStartIfComplete(true)
//                .build();
//    }
//
//    @StepScope
//    @Bean
//    public Tasklet validatedParamTasklet(@Value("#{jobParameters['fileName']}") String fileName) {
//        return new Tasklet() {
//            @Override
//            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//                System.out.println("fileName = " + fileName);
//                System.out.println("validated Param Tasklet");
//                return RepeatStatus.FINISHED; // FINISHED를 명시함으로써 이 Step을 끝낸다는 뜻.
//            }
//        };
//    }
//
//}
