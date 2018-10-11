package com.example.quartz.controller;

import com.example.quartz.entity.SimpleJob;
import com.example.quartz.job.BaseJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/job")
public class JobController {
    private static Logger log = LoggerFactory.getLogger(JobController.class);
    //加入Qulifier注解，通过名称注入bean
    @Autowired
    @Qualifier("Scheduler")
    private Scheduler scheduler;

    private static BaseJob getClass(String classname) throws Exception {
        Class<?> class1 = Class.forName(classname);
        return (BaseJob) class1.getDeclaredConstructor().newInstance();
    }

    @PostMapping(value = "/addJob")
    public void addJob(@RequestBody SimpleJob simpleJob) throws Exception {
        addJobInternal(simpleJob);
    }

    private void addJobInternal(SimpleJob simpleJob) throws Exception {
        // 启动调度器
        scheduler.start();

        //构建job信息
        JobDetail jobDetail = JobBuilder.newJob(getClass(simpleJob.getJobClassName()).getClass()).withIdentity(simpleJob.getJobClassName(), simpleJob.getJobGroupName()).build();

        //表达式调度构建器(即任务执行的时间)
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(simpleJob.getCronExpression());

        //按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(simpleJob.getJobClassName(), simpleJob.getJobGroupName())
                .withSchedule(scheduleBuilder).build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            System.out.println("创建定时任务失败" + e);
            throw new Exception("创建定时任务失败");
        }
    }

    @PostMapping(value = "/pauseJob")
    public void pauseJob(@RequestParam(value = "jobClassName") String jobClassName, @RequestParam(value = "jobGroupName") String jobGroupName) throws Exception {
        pauseJobInternal(jobClassName, jobGroupName);
    }

    private void pauseJobInternal(String jobClassName, String jobGroupName) throws Exception {
        scheduler.pauseJob(JobKey.jobKey(jobClassName, jobGroupName));
    }

    @PostMapping(value = "/resumeJob")
    public void resumeJob(@RequestParam(value = "jobClassName") String jobClassName, @RequestParam(value = "jobGroupName") String jobGroupName) throws Exception {
        resumeJobInternal(jobClassName, jobGroupName);
    }

    private void resumeJobInternal(String jobClassName, String jobGroupName) throws Exception {
        scheduler.resumeJob(JobKey.jobKey(jobClassName, jobGroupName));
    }

    @PostMapping(value = "/rescheduleJob")
    public void rescheduleJob(@RequestParam(value = "jobClassName") String jobClassName,
                              @RequestParam(value = "jobGroupName") String jobGroupName,
                              @RequestParam(value = "cronExpression") String cronExpression) throws Exception {
        rescheduleJobInternal(jobClassName, jobGroupName, cronExpression);
    }

    private void rescheduleJobInternal(String jobClassName, String jobGroupName, String cronExpression) throws Exception {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobClassName, jobGroupName);
            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            System.out.println("更新定时任务失败" + e);
            throw new Exception("更新定时任务失败");
        }
    }

    @PostMapping(value = "/deleteJob")
    public void deleteJob(@RequestParam(value = "jobClassName") String jobClassName, @RequestParam(value = "jobGroupName") String jobGroupName) throws Exception {
        deleteJobInternal(jobClassName, jobGroupName);
    }

    private void deleteJobInternal(String jobClassName, String jobGroupName) throws Exception {
        scheduler.pauseTrigger(TriggerKey.triggerKey(jobClassName, jobGroupName));
        scheduler.unscheduleJob(TriggerKey.triggerKey(jobClassName, jobGroupName));
        scheduler.deleteJob(JobKey.jobKey(jobClassName, jobGroupName));
    }

    @GetMapping(value = "/queryJob")
    public List<Map<String, Object>> queryJob() throws SchedulerException {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String group : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                Map<String, Object> map = new HashMap<>();
                result.add(map);
                map.put("jobClass", scheduler.getJobDetail(jobKey).getJobClass());
                map.put("jobCron", ((CronTrigger) scheduler.getTriggersOfJob(jobKey).get(0)).getCronExpression());
            }
        }
        return result;
    }
}