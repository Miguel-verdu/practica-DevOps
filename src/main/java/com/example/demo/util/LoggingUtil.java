package com.example.demo.util;

import org.slf4j.MDC;
import java.util.UUID;

/**
 * Clase utilitaria para logs seguros sin datos sensibles
 */
public class LoggingUtil {

    /**
     * Genera un ID único para rastrear operaciones en toda la aplicación
     */
    public static String generarIdOperacion() {
        String idOperacion = UUID.randomUUID().toString();
        MDC.put("operacion_id", idOperacion);
        return idOperacion;
    }

    /**
     * Establece el usuario en contexto para logging
     */
    public static void establecerUsuario(String usuario) {
        // Sin exponer datos sensibles, solo usar identificador
        MDC.put("usuario_id", usuario != null ? usuario.hashCode() + "" : "anonimo");
    }

    /**
     * Registra inicio de operación de manera segura
     */
    public static String registrarInicio(String operacion) {
        String idOp = generarIdOperacion();
        return String.format("[%s] Iniciando: %s", idOp, operacion);
    }

    /**
     * Enmascarar valores sensibles (emails, teléfonos, etc.)
     */
    public static String enmascarar(String valor) {
        if (valor == null || valor.length() <= 2) {
            return "***";
        }
        return valor.substring(0, 2) + "*".repeat(Math.max(0, valor.length() - 4)) + 
               (valor.length() > 2 ? valor.substring(valor.length() - 2) : "");
    }

    /**
     * Limpia el contexto de logging
     */
    public static void limpiarContexto() {
        MDC.clear();
    }
}
