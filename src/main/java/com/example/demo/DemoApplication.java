package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación de gestión de tickets.
 *
 * <p>Punto de entrada de la aplicación Spring Boot. Al ejecutarse,
 * arranca el contexto de Spring, configura automáticamente todos los
 * componentes detectados por escaneo de paquetes y levanta el servidor
 * web embebido (Tomcat por defecto).</p>
 *
 * <p>La anotación {@code @SpringBootApplication} equivale a combinar:</p>
 * <ul>
 *   <li>{@code @Configuration} – declara esta clase como fuente de beans.</li>
 *   <li>{@code @EnableAutoConfiguration} – activa la auto-configuración de Spring Boot.</li>
 *   <li>{@code @ComponentScan} – escanea el paquete {@code com.example.demo} y sus subpaquetes.</li>
 * </ul>
 *
 * @author Miguel Verdú Pacheco
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class DemoApplication {

	/**
	 * Método principal que inicia la aplicación Spring Boot.
	 *
	 * <p>Delega en {@link SpringApplication#run(Class, String[])} para
	 * arrancar el contexto de aplicación y el servidor embebido.</p>
	 *
	 * @param args argumentos de línea de comandos opcionales que Spring Boot
	 *             puede usar para sobrescribir propiedades de configuración
	 *             (por ejemplo, {@code --server.port=9090}).
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
