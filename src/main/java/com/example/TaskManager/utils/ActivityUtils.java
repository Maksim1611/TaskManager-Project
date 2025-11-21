package com.example.TaskManager.utils;

import com.example.TaskManager.activity.model.Activity;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class ActivityUtils {

    public static void setActivityCreatedDateFormatted(Activity activity) {
        LocalDateTime createdOn = activity.getCreatedOn();
        Duration duration = Duration.between(createdOn, LocalDateTime.now());

        if (duration.toSeconds() < 60) {
            activity.setDateOutput("Just now");
        } else if (duration.toMinutes() < 60) {
            activity.setDateOutput(String.format("%d minutes ago", duration.toMinutes()));
        } else if (duration.toHours() < 24) {
            activity.setDateOutput(String.format("%d hours ago", duration.toHours()));
        } else if (duration.toDays() == 1) {
            activity.setDateOutput("Yesterday");
        } else {
            String format = createdOn.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            activity.setDateOutput(format);
        }
    }

}
