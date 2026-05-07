package com.collectorhub.backend.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import org.springframework.http.HttpHeaders;


import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    // Pon aquí tus credenciales de EmailJS
    private final String SERVICE_ID = "service_tavecfb";
    private final String TEMPLATE_ID = "template_yg9kc5h";
    private final String PUBLIC_KEY = "OG-5oIkZLJft-0ROe";
    private final String PRIVATE_KEY = "VQkZZi1Fzrcz4v4vQUsNV";

    @Async
    public void enviarCorreoAPI(String emailDestino, String nombreUsuario, String pin) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.emailjs.com/api/v1.0/email/send";

        // Preparamos los parámetros del template
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("to_name", nombreUsuario);
        templateParams.put("user_email", emailDestino);
        templateParams.put("pin", pin);

        // Preparamos el cuerpo de la petición según la documentación de EmailJS
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("service_id", SERVICE_ID);
        requestBody.put("template_id", TEMPLATE_ID);
        requestBody.put("user_id", PUBLIC_KEY);
        requestBody.put("accessToken", PRIVATE_KEY); // Requerido para llamadas desde el servidor
        requestBody.put("template_params", templateParams);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
            System.out.println("PIN enviado con éxito mediante EmailJS");
        } catch (Exception e) {
            System.err.println("Error en EmailJS: " + e.getMessage());
            throw e;
        }
    }
}