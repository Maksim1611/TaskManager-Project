# TaskFlow

TaskFlow is a task and project management system that helps users organize their work efficiently.  
Users can manage personal tasks, create their own projects, or collaborate in groups.  
The application includes Activity tracking, Analytics, Notifications, and a clean modern interface designed to improve productivity.

---

## Features

### User & Roles
- User registration and login
- Profile and personalized dashboard
- Three User Roles:
     - **User** – basic functionalities
     - **Moderator** – has access to send Alerts to the users
     - **Admin** – has full control over the users(e.g., delete and block users, switching roles, etc.)

### Projects
- Create, edit, delete projects
- Invite or remove members
- Project statuses: Active, On Hold, Completed, Canceled
- Progress bar based on completed tasks
- Detailed project page with tasks, analytics, and activity
- Two project roles:
    - **Owner** – full control over tasks and members
    - **Member** – read-only access inside shared projects

###  Tasks
- Create, edit, delete tasks (owners only)
- Set priority, status, due date, and tags
- Tasks grouped by status (To-Do / In Progress / Completed)
- Clean UI with colored labels and quick actions

### Analytics
- Overall project completion rate
- Lifetime statistics for users
- Completed vs. active tasks
- Upcoming deadlines

### Activity & Notifications
- Activity timeline for each project
- System notifications for important events
- Visual alerts for deadlines and progress

---

## Technologies Used
- **Java 17**
- **Spring Boot**
- **Spring MVC**
- **Spring Security**
- **Spring Security OAuth2**
- **OAuth2 (Google & GitHub Providers)**
- **Spring Data JPA (Hibernate)**
- **MySQL**
- **Thymeleaf**
- **HTML / CSS / Bootstrap**

---

## Project Structure
The project follows a clean-layered architecture:
- **Controllers** – handle HTTP requests
- **Services** – business logic
- **Repositories** – database operations
- **Models/Entities** – data objects
- **Views** – Thymeleaf templates and UI

This structure keeps the code easy to maintain and extend.
