package com.example.batch.core.domain.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UsersRepository extends JpaRepository<Users, Integer> {
    @Query("SELECT u FROM Users u WHERE u.status = 'NEW_USER' OR u.status = 'USER'")
    Page<Users> findNewUserAndUser(Pageable pageable);
}
