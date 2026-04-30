package com.collectorhub.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoPin(String correoDestino, String alias, String pin) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correoDestino);
        mensaje.setSubject("🔑 Tu código de seguridad - CollectorHub");

        mensaje.setText("Hola " + alias + ",\n\n"
                + "Gracias por registrarte en CollectorHub. Para terminar de crear tu cuenta y comprobar que este correo es tuyo, introduce el siguiente código de 6 dígitos en la aplicación:\n\n"
                + "CÓDIGO DE VERIFICACIÓN: " + pin + "\n\n"
                + "Si no has sido tú, simplemente ignora este correo.\n\n"
                + "El equipo de CollectorHub.");

        mailSender.send(mensaje);
    }
}