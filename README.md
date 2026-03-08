# 🎫 Sistema de Gestión de Tickets

Aplicación REST desarrollada con **Spring Boot** y **Gradle** que permite crear, consultar, actualizar y eliminar tickets de soporte técnico. Incluye un sistema de diagnóstico y monitoreo del estado de la aplicación.

---

## 📋 Índice

- [Descripción del proyecto](#descripción-del-proyecto)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Cómo clonar y usar el repositorio](#cómo-clonar-y-usar-el-repositorio)
- [Documentación HTML](#documentación-html)
- [Workflow de publicación CI/CD](#workflow-de-publicación-cicd)
- [Ejemplos de código documentado](#ejemplos-de-código-documentado)
- [Mensajes de commit](#mensajes-de-commit)
- [Cuestionario de evaluación](#cuestionario-de-evaluación)
- [Conclusiones](#conclusiones)

---

## Descripción del proyecto

El sistema permite gestionar el ciclo de vida completo de un ticket de soporte:

- **Crear** tickets con título, descripción y estado inicial `ABIERTO`
- **Consultar** todos los tickets o uno concreto por ID
- **Actualizar** el estado de un ticket (`ABIERTO` → `EN_PROCESO` → `RESUELTO` → `CERRADO`)
- **Eliminar** tickets existentes
- **Monitorear** el estado del sistema mediante endpoints de diagnóstico

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.x | Framework web |
| Gradle | 8.14 | Gestión de dependencias y build |
| JavaDoc | (incluido en JDK) | Generación de documentación |
| GitHub Actions | - | CI/CD automatizado |
| GitHub Pages | - | Publicación de documentación |

---

## Cómo clonar y usar el repositorio

### 1. Clonar el repositorio

```bash
git clone https://github.com/Miguel-verdu/RecuperacionP01-MVP.git
cd RecuperacionP01-MVP
```

### 2. Requisitos previos

- Tener instalado **JDK 21** → [Descargar en adoptium.net](https://adoptium.net)
- No es necesario instalar Gradle; el proyecto incluye el wrapper `gradlew`

### 3. Ejecutar la aplicación

```bash
./gradlew bootRun
```

La aplicación arrancará en `http://localhost:8080`

### 4. Regenerar la documentación JavaDoc localmente

```bash
./gradlew javadoc
```

La documentación se genera en:

```
build/docs/javadoc/index.html
```

Ábrela directamente con tu navegador haciendo doble clic sobre el archivo `index.html`.

---

## Documentación HTML

### a) Herramienta usada y comandos ejecutados

La herramienta utilizada para generar la documentación HTML es **JavaDoc**, incluida en el propio JDK de Java. Es el estándar oficial para documentar proyectos Java.

La documentación se genera mediante Gradle con el siguiente comando:

```bash
./gradlew javadoc
```

Este comando lee los comentarios `/** ... */` de todos los archivos `.java` del proyecto y genera un sitio web HTML navegable con la documentación de clases, métodos y atributos.

**Ruta de salida local:**
```
build/docs/javadoc/index.html
```

### b) Ejemplos de código documentado

El estilo de documentación utilizado es **JavaDoc estándar**, que es el formato oficial de Java. Utiliza comentarios especiales `/** ... */` con etiquetas que comienzan por `@`.

**Etiquetas utilizadas:**

| Etiqueta | Descripción |
|---|---|
| `@author` | Autor de la clase |
| `@version` | Versión del componente |
| `@since` | Versión desde la que existe |
| `@param` | Describe cada parámetro de un método |
| `@return` | Describe el valor de retorno |
| `@throws` | Describe las excepciones que puede lanzar |
| `@see` | Enlace a otra clase o método relacionado |

**Fragmento de ejemplo — método con `@param`, `@return` y `@throws`:**

```java
/**
 * Actualiza el estado de un ticket existente.
 *
 * @param id          identificador único del ticket a actualizar.
 * @param nuevoEstado nuevo valor de {@link EstadoTicket} a asignar.
 * @return el {@link Ticket} con el estado actualizado.
 * @throws IllegalArgumentException si no existe ningún ticket con ese ID.
 */
public Ticket actualizarEstado(Long id, EstadoTicket nuevoEstado) { ... }
```

**Fragmento de ejemplo — clase con `@author` y `@version`:**

```java
/**
 * Controlador REST para la gestión completa de tickets de soporte.
 *
 * @author Miguel Verdú Pacheco
 * @version 1.0
 * @since 1.0
 * @see TicketService
 */
@RestController
@RequestMapping("/tickets")
public class HolaController { ... }
```

**Fragmento de ejemplo — enum documentado:**

```java
/**
 * El ticket ha sido creado y está pendiente de ser atendido.
 * Es el estado inicial asignado automáticamente al crear un ticket.
 */
ABIERTO,
```

**Enlace al código fuente documentado:**
- [`HolaController.java`](src/main/java/com/example/demo/HolaController.java)
- [`TicketService.java`](src/main/java/com/example/demo/service/TicketService.java)
- [`Ticket.java`](src/main/java/com/example/demo/entity/Ticket.java)
- [`EstadoTicket.java`](src/main/java/com/example/demo/entity/EstadoTicket.java)
- [`DiagnosticService.java`](src/main/java/com/example/demo/service/DiagnosticService.java)
- [`GlobalExceptionHandler.java`](src/main/java/com/example/demo/GlobalExceptionHandler.java)

### c) Documentación pública en GitHub Pages

La documentación JavaDoc generada automáticamente está publicada y accesible en:

🔗 **[https://miguel-verdu.github.io/RecuperacionP01-MVP/](https://miguel-verdu.github.io/RecuperacionP01-MVP/)**

---

## Workflow de publicación CI/CD

### d) Explicación del workflow

El workflow está definido en [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml) y se encarga de generar y publicar la documentación de forma automática.

**Evento que lo dispara:**

```yaml
on:
  push:
    branches: [ main ]
```

Cada vez que se hace `git push` a la rama `main`, el workflow se ejecuta automáticamente.

**Pasos del job:**

| Paso | Acción | Descripción |
|---|---|---|
| 1 | `actions/checkout@v3` | Descarga el código del repositorio en el servidor de GitHub |
| 2 | `actions/setup-java@v3` | Instala Java 21 (distribución Temurin) en el servidor |
| 3 | `chmod +x gradlew` | Da permisos de ejecución al wrapper de Gradle (necesario en Linux) |
| 4 | `./gradlew javadoc` | Genera la documentación HTML con JavaDoc |
| 5 | `peaceiris/actions-gh-pages@v3` | Publica el contenido de `build/docs/javadoc` en la rama `gh-pages` |

**Acción `peaceiris/actions-gh-pages@v3`:**

Esta acción toma la carpeta indicada en `publish_dir` y la sube automáticamente a la rama `gh-pages` del repositorio. GitHub Pages sirve esta rama como sitio web público. Usa el token `GITHUB_TOKEN` (generado automáticamente por GitHub) para tener permisos de escritura.

**Archivo completo del workflow:**

```yaml
name: Generar y Publicar JavaDoc

on:
  push:
    branches: [ main ]

jobs:
  javadoc:
    runs-on: ubuntu-latest

    steps:
      - name: Descargar código
        uses: actions/checkout@v3

      - name: Instalar Java 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Dar permisos a gradlew
        run: chmod +x gradlew

      - name: Generar JavaDoc con Gradle
        run: ./gradlew javadoc

      - name: Publicar en GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./build/docs/javadoc
```

---

## Mensajes de commit

### e) Commits que evidencian la configuración del workflow

A continuación se muestran los mensajes de commit realizados durante la configuración de la documentación y el workflow:

```
Añadir comentarios JavaDoc a todas las clases y métodos del proyecto
Añadir workflow de GitHub Actions para generación y publicación de JavaDoc
Fix: añadir permisos de ejecución a gradlew en el workflow
Fix: cambiar Java 25 a 21 en build.gradle y workflow por incompatibilidad con Gradle
Fix: actualizar Gradle a 8.14 para compatibilidad con Java 21
Fix: activar permisos de escritura en GitHub Actions para publicar en gh-pages
```

Los mensajes están escritos en **modo imperativo** ("Añadir", "Fix", "Actualizar"), son descriptivos e indican exactamente qué se hizo y por qué, siguiendo las buenas prácticas de control de versiones.

---

## Cuestionario de evaluación

### a) ¿Qué herramienta utilizaste para crear la documentación HTML?

Se utilizó **JavaDoc**, la herramienta oficial incluida en el JDK de Java. Se ejecuta a través de Gradle con el comando `./gradlew javadoc`, que invoca internamente la tarea `javadoc` del plugin de Java de Gradle. JavaDoc lee los comentarios `/** ... */` del código fuente y genera un sitio web HTML estático completamente navegable.

---

### b) Muestra un fragmento de código documentado y comenta el estilo utilizado

El estilo utilizado es **JavaDoc estándar**, el formato oficial y más extendido en el ecosistema Java.

```java
/**
 * Persiste un nuevo ticket en la base de datos.
 *
 * @param ticket objeto {@link Ticket} con los datos a persistir.
 * @return el {@link Ticket} recién guardado con el ID asignado.
 * @throws RuntimeException si ocurre algún error durante la persistencia.
 */
public Ticket crearTicket(Ticket ticket) {
    Ticket nuevoTicket = ticketRepository.save(ticket);
    return nuevoTicket;
}
```

El comentario comienza con `/**` y cierra con `*/`. La primera parte es la descripción en texto libre. Las etiquetas `@param`, `@return` y `@throws` aportan información estructurada que JavaDoc convierte en tablas HTML en la documentación generada. La etiqueta `{@link}` crea hipervínculos entre clases dentro de la propia documentación.

---

### c) ¿Qué configuración utilizaste para publicar en GitHub Pages?

Se realizaron dos configuraciones:

**En el workflow** (`.github/workflows/javadoc.yml`): se usa la acción `peaceiris/actions-gh-pages@v3`, que toma la carpeta `./build/docs/javadoc` generada por Gradle y la sube automáticamente a la rama `gh-pages` del repositorio usando el `GITHUB_TOKEN`.

**En GitHub** (Settings → Pages): se configuró la fuente de publicación como la rama `gh-pages`, carpeta raíz `/`. A partir de ese momento, GitHub sirve automáticamente el contenido de esa rama como un sitio web público en `https://miguel-verdu.github.io/RecuperacionP01-MVP/`.

También fue necesario activar **permisos de escritura** para GitHub Actions en Settings → Actions → General → Workflow permissions → Read and write permissions.

---

### d) ¿Qué ventajas tiene GitHub Pages frente a solo tener los archivos HTML en el repositorio?

GitHub Pages convierte los archivos HTML en un sitio web accesible desde cualquier navegador sin necesidad de descargar nada. Las ventajas frente a tener solo los archivos en el repositorio son:

- **Accesibilidad inmediata**: cualquier persona puede ver la documentación desde una URL pública sin tener Git ni Java instalados.
- **Siempre actualizada**: al estar integrado con el workflow de CI/CD, cada `push` regenera y republica la documentación automáticamente, sin intervención manual.
- **Facilita la colaboración**: los compañeros de equipo, clientes o usuarios externos pueden consultar la API directamente desde el navegador.
- **Sin fricción**: no hay que clonar el repositorio, instalar dependencias ni ejecutar ningún comando para ver la documentación.

---

### e) Muestra mensajes de commit que evidencien la configuración del workflow

```
Añadir workflow de GitHub Actions para generación y publicación de JavaDoc
Fix: añadir permisos de ejecución a gradlew en el workflow
Fix: cambiar Java 25 a 21 en build.gradle y workflow por incompatibilidad con Gradle
Fix: activar permisos de escritura en GitHub Actions para publicar en gh-pages
```

Sí son claros y descriptivos porque indican exactamente **qué se hizo** ("añadir permisos", "cambiar Java 25 a 21") y en los commits de corrección también **por qué** ("por incompatibilidad con Gradle"). Están escritos en imperativo, que es el formato recomendado por las convenciones de Git, y permiten entender la evolución del proyecto sin necesidad de abrir cada commit.

---

### f) ¿Cómo garantizas que la documentación es pública pero el código fuente está protegido?

GitHub permite configurar la visibilidad del repositorio y de GitHub Pages de forma independiente:

- Si el **repositorio es privado**, solo los colaboradores autorizados pueden ver el código fuente, las issues, los commits y el historial.
- **GitHub Pages puede seguir siendo público** aunque el repositorio sea privado (disponible en planes de pago) o si el repositorio es público, Pages es público por defecto.

En este proyecto el repositorio es público, por lo que tanto el código como la documentación son accesibles. Si se quisiera proteger el código, bastaría con cambiar la visibilidad del repositorio a privado en Settings → General → Danger Zone → Change visibility, manteniendo Pages activo para la documentación.

---

### g) ¿Dónde en el README se explica cómo acceder a la documentación y cómo generarla?

- **Acceso a la documentación publicada**: sección [Documentación HTML → apartado c)](#c-documentación-pública-en-github-pages), donde aparece el enlace directo a GitHub Pages.
- **Herramientas y comandos para generarla localmente**: sección [Documentación HTML → apartado a)](#a-herramienta-usada-y-comandos-ejecutados) y sección [Cómo clonar y usar el repositorio → paso 4](#4-regenerar-la-documentación-javadoc-localmente).

---

### h) ¿Por qué el workflow implementa CI/CD?

El workflow implementa **CI/CD** (Integración Continua y Despliegue Continuo) por las siguientes razones:

**Integración Continua (CI):** cada vez que se hace `push` a `main`, el workflow se ejecuta automáticamente, compila el proyecto y genera la documentación. Esto garantiza que la documentación esté siempre sincronizada con el código y que cualquier error en la generación se detecte de inmediato.

**Despliegue Continuo (CD):** tras generar la documentación, el workflow la publica automáticamente en GitHub Pages sin intervención humana. El resultado está disponible públicamente en cuestión de minutos tras cada `push`.

**El evento que dispara todo el proceso** es:
```yaml
on:
  push:
    branches: [ main ]
```

Esto es despliegue continuo porque el paso desde el código hasta la documentación publicada y accesible públicamente es completamente automático: el desarrollador solo tiene que hacer `git push` y el resto lo gestiona el workflow.

---

## Conclusiones

Durante el desarrollo de esta práctica he aprendido y consolidado los siguientes conceptos:

**Sobre JavaDoc:** es una herramienta muy potente incluida en el propio JDK que permite generar documentación profesional directamente desde los comentarios del código. Usar etiquetas estructuradas como `@param`, `@return` y `@throws` no solo genera HTML útil, sino que también mejora la legibilidad del código para otros desarrolladores.

**Sobre GitHub Actions:** permite automatizar cualquier tarea del ciclo de vida del software mediante workflows declarados en archivos YAML. He aprendido que pequeños detalles como los permisos de los archivos (`chmod`) o la compatibilidad entre versiones de Java y Gradle son críticos para que el workflow funcione correctamente.

**Sobre GitHub Pages:** es una solución gratuita y muy sencilla para publicar documentación estática. Al integrarlo con GitHub Actions, conseguimos que la documentación se publique automáticamente con cada cambio en el código, eliminando cualquier proceso manual.

**Sobre CI/CD:** he comprendido en la práctica qué significa la integración y el despliegue continuos. Un simple `git push` desencadena toda la cadena: generación de documentación → publicación en Pages → disponibilidad pública. Esto es exactamente lo que busca el CI/CD: reducir la fricción entre escribir código y tenerlo disponible para los usuarios.
