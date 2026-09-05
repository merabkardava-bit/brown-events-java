package com.brownevents.app;

import com.brownevents.app.entity.Conference;
import com.brownevents.app.entity.Room;
import com.brownevents.app.entity.Session;
import com.brownevents.app.entity.Speaker;
import com.brownevents.app.repository.ConferenceRepository;
import com.brownevents.app.repository.RoomRepository;
import com.brownevents.app.repository.SessionRepository;
import com.brownevents.app.repository.SpeakerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ConferenceRepository conferenceRepository;
    private final SpeakerRepository speakerRepository;
    private final RoomRepository roomRepository;
    private final SessionRepository sessionRepository;

    public DataInitializer(ConferenceRepository conferenceRepository,
                           SpeakerRepository speakerRepository,
                           RoomRepository roomRepository,
                           SessionRepository sessionRepository) {
        this.conferenceRepository = conferenceRepository;
        this.speakerRepository = speakerRepository;
        this.roomRepository = roomRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (conferenceRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        // Create speakers
        Speaker john = new Speaker();
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setBio("Senior Java Developer with 15 years of experience in enterprise applications.");
        john.setEmail("john.doe@example.com");
        john = speakerRepository.save(john);

        Speaker jane = new Speaker();
        jane.setFirstName("Jane");
        jane.setLastName("Smith");
        jane.setBio("Cloud architect specializing in Kubernetes and distributed systems.");
        jane.setEmail("jane.smith@example.com");
        jane = speakerRepository.save(jane);

        Speaker bob = new Speaker();
        bob.setFirstName("Bob");
        bob.setLastName("Wilson");
        bob.setBio("DevOps engineer and open source contributor focused on CI/CD and infrastructure automation.");
        bob.setEmail("bob.wilson@example.com");
        bob = speakerRepository.save(bob);

        // Create rooms
        Room mainHall = new Room();
        mainHall.setName("Main Hall");
        mainHall.setCapacity(500);
        mainHall.setLocation("Ground Floor");
        mainHall = roomRepository.save(mainHall);

        Room roomA = new Room();
        roomA.setName("Room A");
        roomA.setCapacity(100);
        roomA.setLocation("1st Floor");
        roomA = roomRepository.save(roomA);

        Room roomB = new Room();
        roomB.setName("Room B");
        roomB.setCapacity(80);
        roomB.setLocation("1st Floor");
        roomB = roomRepository.save(roomB);

        Room workshopRoom = new Room();
        workshopRoom.setName("Workshop Room");
        workshopRoom.setCapacity(30);
        workshopRoom.setLocation("2nd Floor");
        workshopRoom = roomRepository.save(workshopRoom);

        // Create conferences
        Conference springTech = new Conference();
        springTech.setTitle("Spring Tech Summit 2024");
        springTech.setDescription("The premier conference for Spring Framework enthusiasts and Java developers.");
        springTech.setLocation("San Francisco, CA");
        springTech.setStartDate(LocalDate.of(2024, 3, 15));
        springTech.setEndDate(LocalDate.of(2024, 3, 17));
        springTech.setStatus("UPCOMING");
        springTech = conferenceRepository.save(springTech);

        Conference javaDays = new Conference();
        javaDays.setTitle("Java Developer Days");
        javaDays.setDescription("A community-driven conference for Java professionals.");
        javaDays.setLocation("New York, NY");
        javaDays.setStartDate(LocalDate.of(2024, 5, 20));
        javaDays.setEndDate(LocalDate.of(2024, 5, 22));
        javaDays.setStatus("ACTIVE");
        javaDays = conferenceRepository.save(javaDays);

        Conference cloudDevOps = new Conference();
        cloudDevOps.setTitle("Cloud & DevOps World");
        cloudDevOps.setDescription("Explore the latest in cloud computing and DevOps practices.");
        cloudDevOps.setLocation("Austin, TX");
        cloudDevOps.setStartDate(LocalDate.of(2023, 9, 10));
        cloudDevOps.setEndDate(LocalDate.of(2023, 9, 12));
        cloudDevOps.setStatus("COMPLETED");
        cloudDevOps = conferenceRepository.save(cloudDevOps);

        // Sessions for Spring Tech Summit 2024
        Session session1 = new Session();
        session1.setTitle("Keynote: The Future of Spring");
        session1.setDescription("An overview of the Spring ecosystem roadmap and new features.");
        session1.setStartTime(LocalDateTime.of(2024, 3, 15, 9, 0));
        session1.setEndTime(LocalDateTime.of(2024, 3, 15, 10, 0));
        session1.setCapacity(500);
        session1.setConference(springTech);
        session1.setSpeaker(john);
        session1.setRoom(mainHall);
        sessionRepository.save(session1);

        Session session2 = new Session();
        session2.setTitle("Spring Boot 3 Deep Dive");
        session2.setDescription("Hands-on exploration of Spring Boot 3 features and migration path.");
        session2.setStartTime(LocalDateTime.of(2024, 3, 15, 11, 0));
        session2.setEndTime(LocalDateTime.of(2024, 3, 15, 12, 30));
        session2.setCapacity(100);
        session2.setConference(springTech);
        session2.setSpeaker(jane);
        session2.setRoom(roomA);
        sessionRepository.save(session2);

        Session session3 = new Session();
        session3.setTitle("Reactive Programming Workshop");
        session3.setDescription("Practical workshop on Spring WebFlux and reactive patterns.");
        session3.setStartTime(LocalDateTime.of(2024, 3, 16, 14, 0));
        session3.setEndTime(LocalDateTime.of(2024, 3, 16, 17, 0));
        session3.setCapacity(30);
        session3.setConference(springTech);
        session3.setSpeaker(bob);
        session3.setRoom(workshopRoom);
        sessionRepository.save(session3);

        // Sessions for Java Developer Days
        Session session4 = new Session();
        session4.setTitle("Modern Java Features: Records, Sealed Classes, and Pattern Matching");
        session4.setDescription("Exploring the latest Java language features and how to use them effectively.");
        session4.setStartTime(LocalDateTime.of(2024, 5, 20, 10, 0));
        session4.setEndTime(LocalDateTime.of(2024, 5, 20, 11, 30));
        session4.setCapacity(100);
        session4.setConference(javaDays);
        session4.setSpeaker(john);
        session4.setRoom(roomA);
        sessionRepository.save(session4);

        Session session5 = new Session();
        session5.setTitle("Microservices Architecture Patterns");
        session5.setDescription("Best practices and patterns for building resilient microservices.");
        session5.setStartTime(LocalDateTime.of(2024, 5, 21, 13, 0));
        session5.setEndTime(LocalDateTime.of(2024, 5, 21, 14, 30));
        session5.setCapacity(80);
        session5.setConference(javaDays);
        session5.setSpeaker(jane);
        session5.setRoom(roomB);
        sessionRepository.save(session5);

        // Sessions for Cloud & DevOps World
        Session session6 = new Session();
        session6.setTitle("Kubernetes in Production");
        session6.setDescription("Lessons learned from running Kubernetes clusters at scale.");
        session6.setStartTime(LocalDateTime.of(2023, 9, 10, 9, 30));
        session6.setEndTime(LocalDateTime.of(2023, 9, 10, 11, 0));
        session6.setCapacity(500);
        session6.setConference(cloudDevOps);
        session6.setSpeaker(jane);
        session6.setRoom(mainHall);
        sessionRepository.save(session6);

        Session session7 = new Session();
        session7.setTitle("CI/CD Pipeline Best Practices");
        session7.setDescription("Building robust and fast CI/CD pipelines with modern tooling.");
        session7.setStartTime(LocalDateTime.of(2023, 9, 11, 14, 0));
        session7.setEndTime(LocalDateTime.of(2023, 9, 11, 15, 30));
        session7.setCapacity(80);
        session7.setConference(cloudDevOps);
        session7.setSpeaker(bob);
        session7.setRoom(roomB);
        sessionRepository.save(session7);

        Session session8 = new Session();
        session8.setTitle("Infrastructure as Code Workshop");
        session8.setDescription("Hands-on workshop on Terraform and Pulumi for infrastructure automation.");
        session8.setStartTime(LocalDateTime.of(2023, 9, 12, 10, 0));
        session8.setEndTime(LocalDateTime.of(2023, 9, 12, 13, 0));
        session8.setCapacity(30);
        session8.setConference(cloudDevOps);
        session8.setSpeaker(bob);
        session8.setRoom(workshopRoom);
        sessionRepository.save(session8);
    }
}
