package com.hostel.config;

import com.hostel.model.Room;
import com.hostel.model.User;
import com.hostel.repository.RoomRepository;
import com.hostel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Seeds a default admin account and a few sample rooms on first start. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final RoomRepository rooms;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!users.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            users.save(admin);
        }
        if (rooms.count() == 0) {
            seed("A-101", "A", 1, 2, Room.RoomType.DOUBLE, "3500");
            seed("A-102", "A", 1, 3, Room.RoomType.TRIPLE, "2800");
            seed("A-201", "A", 2, 1, Room.RoomType.SINGLE, "5000");
            seed("B-101", "B", 1, 2, Room.RoomType.DOUBLE, "3500");
            seed("B-102", "B", 1, 4, Room.RoomType.DORMITORY, "2000");
        }
    }

    private void seed(String number, String block, int floor, int capacity, Room.RoomType type, String rent) {
        Room r = new Room();
        r.setRoomNumber(number);
        r.setBlock(block);
        r.setFloor(floor);
        r.setCapacity(capacity);
        r.setType(type);
        r.setMonthlyRent(new BigDecimal(rent));
        rooms.save(r);
    }
}
