 ![Java CI with Maven](https://github.com/MateoSmicht/GestionLumato/actions/workflows/maven-test.yml/badge.svg)
# 📈 Sistema de Gestión Comercial & Stock (ERP)

> Aplicación de escritorio completa para la administración de negocios, control de inventario y facturación, desarrollada en **Java** bajo arquitectura **MVC**.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue?style=for-the-badge&logo=mysql&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVC-green?style=for-the-badge)
![Swing](https://img.shields.io/badge/GUI-Swing-red?style=for-the-badge)

## 📋 Descripción

Este sistema fue diseñado para resolver la problemática real de un comercio minorista: la desconexión entre el stock físico, las ventas y la caja diaria. 

A diferencia de proyectos académicos simples, este software implementa lógica de negocio compleja como el cálculo de costos financieros, gestión de usuarios con permisos y persistencia de datos relacional. El proyecto está construido siguiendo estrictamente el patrón de diseño **Modelo-Vista-Controlador (MVC)** para garantizar la escalabilidad y el mantenimiento del código.

## 🚀 Funcionalidades Clave

* **Gestión de Inventario:** ABM (Alta, Baja, Modificación) de productos con control de stock mínimo.
* **Punto de Venta (POS):** Interfaz ágil para cargar ventas, calculando totales y actualizando el stock en tiempo real.
* **Lógica Financiera:**
    * Implementación de cálculo de costos por **PPP (Precio Promedio Ponderado)**.
    * Manejo de precisión decimal (`BigDecimal`) para evitar errores de redondeo en precios.
* **Reportes:** Generación de estadísticas de ventas y movimientos de caja.
* **Seguridad:** Login de usuarios y gestión de sesiones.

## 🏛️ Arquitectura Técnica (MVC)

El código está estructurado para desacoplar responsabilidades:

1.  **Modelo (Model):** Clases POJO (`Producto`, `Venta`) y DAOs (Data Access Objects) que manejan las consultas SQL puras y la conexión a la Base de Datos.
2.  **Vista (View):** Interfaces gráficas construidas con **Java Swing**, diseñadas para ser intuitivas y responsivas.
3.  **Controlador (Controller):** Intermediarios que capturan los eventos de la vista (clics, teclas), ejecutan la lógica de negocio y actualizan el modelo sin que la vista conozca la base de datos.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java SE (JDK 17/21).
* **Base de Datos:** MySQL / MariaDB.
* **Interfaz:** Java Swing & AWT.
* Gestión de Dependencias: Maven.

Testing: JUnit 5 (Automatizado con GitHub Actions).

Librerías: Gson, JCalendar, iText.

## 📸 Galería
1-Facturacion
<img width="986" height="672" alt="Captura2" src="https://github.com/user-attachments/assets/f51af952-8837-4f45-9ab9-dd4302a5e89c" />
2-Carga de stock
<img width="982" height="674" alt="Captura" src="https://github.com/user-attachments/assets/e3e508bd-9a38-49cb-8d36-fbf56fb9d0d4" />
3-Alta de productos
<img width="984" height="671" alt="Captura3" src="https://github.com/user-attachments/assets/a9afc420-ab7f-4af4-90a9-c1da899e133b" />



## 👤 Autor

**Mateo Damian Smicht**

* **LinkedIn:** https://www.linkedin.com/in/mateosmicht


---
*Proyecto personal desarrollado con fines profesionales y educativos.*
