package com.example.ssauc.admin.repository;

import com.example.ssauc.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByEmailAndPassword(String email, String password);
}