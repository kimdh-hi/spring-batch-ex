package com.spingbatchex.lab7.repository;

import com.spingbatchex.lab7.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findAllByUpdatedAt(LocalDate updatedAt);
}
