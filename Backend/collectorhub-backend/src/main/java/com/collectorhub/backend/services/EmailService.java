package com.collectorhub.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${emailjs.service-id}")
    private String SERVICE_ID;

    @Value("${emailjs.template-id}")
    private String TEMPLATE_ID;

    @Value("${emailjs.public-key}")
    private String PUBLIC_KEY;

    @Value("${emailjs.private-key}")
    private String PRIVATE_KEY;

    public void enviarCorreoAPI(String emailDestino, String nombreUsuario, String pin) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.emailjs.com/api/v1.0/email/send";

        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("to_name", nombreUsuario);
        templateParams.put("user_email", emailDestino);
        templateParams.put("pin", pin);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("service_id", SERVICE_ID);
        requestBody.put("template_id", TEMPLATE_ID);
        requestBody.put("user_id", PUBLIC_KEY);
        requestBody.put("accessToken", PRIVATE_KEY);
        requestBody.put("template_params", templateParams);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
            System.out.println("PIN enviado con éxito a: " + emailDestino);
        } catch (HttpClientErrorException e) {
            System.err.println("Error EmailJS - Código: " + e.getStatusCode());
            System.err.println("Respuesta EmailJS: " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            throw e;
        }
    }
}