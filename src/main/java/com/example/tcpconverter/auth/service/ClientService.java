package com.example.tcpconverter.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tcpconverter.auth.repository.ClientRepository;

@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    public boolean authenticate(String clientId, String clientSecret) {
        System.out.println("[?�증?�도] clientId=" + clientId + ", clientSecret=" + clientSecret);
        return clientRepository.findByClientId(clientId)
                .map(client -> {
                    boolean result = client.getClientSecret().equals(clientSecret);
                    System.out.println("[DB조회] clientId 존재, secret ?�치 ?��?: " + result);
                    return result;
                })
                .orElseGet(() -> {
                    System.out.println("[DB조회] clientId ?�음");
                    return false;
                });
    }
} 
