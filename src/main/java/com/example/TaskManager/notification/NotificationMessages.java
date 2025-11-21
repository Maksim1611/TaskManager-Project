package com.example.TaskManager.notification;

public class NotificationMessages {

    public static final String USER_REGISTER_SUBJECT = "Welcome to TaskManager — your productivity starts here!";
    public static final String USER_REGISTER_BODY = "Hello %s,%n%nYour TaskManager account has been created successfully. You can now start organizing your projects, tracking your tasks, and staying on top of your goals — all in one place.%n%nLet’s get things done,\nThe TaskManager Team";
    public static final String PASSWORD_CHANGE_BODY = """
            Hi %s,
            This is a confirmation that your TaskManager account password was successfully changed.
            
            If you made this change, you don’t need to do anything else.
            If you didn’t, please open TaskManager and reset your password from the login screen to secure your account.
            
            Stay safe,
            The TaskManager Team""";

    public static final String PASSWORD_CHANGE_SUBJECT = "Your TaskManager password has been changed";
    public static final String USER_DELETE_SUBJECT = "Your TaskManager account has been deleted";
    public static final String USER_DELETE_BODY = """
            Hi %s,
            
            This is to confirm that your TaskManager account has been permanently deleted, along with all associated data.
            
            If you requested this deletion, no further action is needed.
            If you did not request it, please contact our support team immediately so we can investigate.
            
            Thank you for being part of TaskManager.
            The TaskManager Team""";

    public static final String TASK_OVERDUE_SUBJECT = "“Task ‘%s’ is now overdue. Consider completing or rescheduling it.”";
    public static final String TASK_UPCOMING_DEADLINE = "“Your task ‘%s’ is due in 24 hours. Don’t forget to finish it on time.”";

    public static final String PROJECT_OVERDUE = "“Project ‘%s’ is now overdue. Consider completing or rescheduling it.”";
    public static final String PROJECT_UPCOMING_DEADLINE = "“Project ‘%s’ is due in 24 hours. Don’t forget to finish it on time.”";

    public static final String DAILY_SUMMARY_MESSAGE_SUBJECT = "\uD83C\uDF05 Daily Summary Activity Message";
    public static final String DAILY_SUMMARY_MESSAGE_BODY = """
                        \uD83D\uDCCA Your Daily Productivity Overview:
                        
                        • \uD83D\uDCDD Tasks created: %d
                        • ✅ Tasks completed: %d
                        • ⚠️ Overdue tasks: %d
                        
                        • \uD83D\uDCC1 Projects created: %d
                        • \uD83D\uDCDA Projects completed: %d
                        
                        • \uD83C\uDFAF Task completion rate (last 24h): %.2f%%
                        
                        \uD83D\uDCAA Keep the momentum — tomorrow can be even better! 🚀""";
}
