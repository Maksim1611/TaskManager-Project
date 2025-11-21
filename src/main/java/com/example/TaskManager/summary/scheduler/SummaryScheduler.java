package com.example.TaskManager.summary.scheduler;

import com.example.TaskManager.summary.model.SummaryDto;
import com.example.TaskManager.summary.service.SummaryService;
import com.example.TaskManager.user.model.User;
import com.example.TaskManager.user.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummaryScheduler {

    private final SummaryService summaryService;
    private final UserService userService;

    public SummaryScheduler(SummaryService summaryService, UserService userService) {
        this.summaryService = summaryService;
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void sendDailySummaries() {
        List<User> users = userService.getAllUsers();

        for (User user : users) {
            SummaryDto dto = summaryService.dailySummary(user.getId());
            summaryService.sendDailySummary(user.getId(), dto);
        }
    }
}
