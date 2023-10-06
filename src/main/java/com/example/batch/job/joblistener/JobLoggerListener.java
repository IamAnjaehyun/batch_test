//package com.example.batch.job.joblistener;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.BatchStatus;
//import org.springframework.batch.core.JobExecution;
//import org.springframework.batch.core.JobExecutionListener;
//
//@Slf4j
//public class JobLoggerListener implements JobExecutionListener {
//    private static String BEFORE_MESSAGE = "{} job is Running";
//    private static String AFTER_MESSAGE = "{} job is Done. (Status: {})";
//
//    @Override
//    public void beforeJob(JobExecution jobExecution) {
//        log.info(BEFORE_MESSAGE, jobExecution.getJobInstance().getJobName());
//    }
//
//    @Override
//    public void afterJob(JobExecution jobExecution) {
//        log.info(AFTER_MESSAGE, jobExecution.getJobInstance().getJobName(), jobExecution.getStatus());
//
//        if(jobExecution.getStatus() == BatchStatus.FAILED){
//            //email 같은거 전송 가능
//            log.info("Job is Failed");
//        }
//    }
//}
