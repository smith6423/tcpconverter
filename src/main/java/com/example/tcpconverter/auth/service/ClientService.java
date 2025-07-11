package com.example.tcpconverter.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tcpconverter.auth.repository.ClientRepository;

@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    public boolean authenticate(String clientId, String clientSecret) {
        System.out.println("[?¸ì¦?œë„] clientId=" + clientId + ", clientSecret=" + clientSecret);
        return clientRepository.findByClientId(clientId)
                .map(client -> {
                    boolean result = client.getClientSecret().equals(clientSecret);
                    System.out.println("[DBì¡°íšŒ] clientId ì¡´ì¬, secret ?¼ì¹˜ ?¬ë?: " + result);
                    return result;
                })
                .orElseGet(() -> {
                    System.out.println("[DBì¡°íšŒ] clientId ?†ìŒ");
                    return false;
                });
    }
} 
