package com.example.tcpconverter.auth.repository;

import java.util.Optional;

import com.example.tcpconverter.auth.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByClientId(String clientId);
} 
