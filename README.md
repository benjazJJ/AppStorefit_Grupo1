# 🛍️ StoreFit — App Android

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android Studio](https://img.shields.io/badge/Android%20Studio-Koala%2B-3DDC84?logo=androidstudio&logoColor=white)](https://developer.android.com/studio)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-DB-1976D2)](https://developer.android.com/jetpack/androidx/releases/room)

Aplicación Android de **e-commerce deportivo** para **StoreFit**: catálogo, detalle con variantes, carrito, historial de compras, autenticación con sesión persistente y **panel admin**.  
Construida con **Kotlin + Jetpack Compose (Material 3)** y persistencia local con **Room/DataStore**.

> **Nota:** Este repositorio es de **solo lectura**. Su contenido es informativo y no recibe cambios externos.

---

## 📚 Índice
- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Stack & Requisitos](#-stack--requisitos)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Instalación y ejecución](#-instalación-y-ejecución)
- [Uso rápido](#-uso-rápido)
- [Módulos principales](#-módulos-principales)
- [Roadmap](#-roadmap)
- [Créditos](#-créditos)

---

## ✨ Características
- **Catálogo** con variantes (color/talla), stock y precio.
- **Carrito**: sumar/restar unidades, eliminar al llegar a 0, subtotal y total.
- **Autenticación y sesión** con **DataStore**; **rutas por rol** (cliente/admin).
- **Historial de compras** con tarjetas Material 3.
- **Panel Admin**: categorías, productos y base para reportes.
- **Arquitectura limpia** (UI / Domain / Data), `ViewModel` + `StateFlow`.
- **Offline-first**: Cache local.

---

## 🧭 Arquitectura
app/
├─ ui/ # Pantallas Compose, componentes y estados UI
│ ├─ components/
│ └─ screen/
├─ navigation/ # Rutas, NavHost y destinos protegidos por rol
├─ data/
│ ├─ local/ # Room (DAO, Entities, Database)
│ ├─ store/ # DataStore (sesión, ajustes)
│ └─ repository/ # Repositorios: fuente de verdad
├─ domain/ # Modelos de dominio / use cases (si aplica)
└─ util/ # Formateadores, Result, extensiones, etc.


**Principios**
- **UI**: Compose + Material 3 (temas, tipografías, dark mode).
- **Estado**: `ViewModel` + `StateFlow` + `collectAsStateWithLifecycle`.
- **Datos**: `Repository` → `Room` (y API cuando aplique).
- **Sesión**: `DataStore`.

---

## 🧰 Stack & Requisitos
- **Kotlin** 1.9+
- **Android Studio** Koala o superior
- **minSdk** 24 • **targetSdk** 34
- **Jetpack**: Compose, Navigation, Lifecycle
- **Room**, **DataStore**

---

## 🗂️ Estructura del proyecto (orientativa)
com.example.appstorefit_grupo1
├─ data
│ ├─ local
│ │ ├─ database/AppDatabase.kt
│ │ ├─ dao/.kt
│ │ └─ entities/.kt
│ ├─ repository/.kt
│ └─ store/SessionManager.kt
├─ navigation/.kt
├─ ui
│ ├─ components/.kt
│ └─ screen/.kt
├─ util/*.kt
└─ MainActivity.kt


> Los nombres concretos pueden variar según el último commit.

---

## 🚀 Instalación y ejecución

1. **Clonar**
   ```bash
   git clone https://github.com/<usuario>/AppStorefit_Grupo1.git
   cd AppStorefit_Grupo1
Abrir en Android Studio
File → Open… y selecciona la carpeta del repo.

Sincronizar Gradle
Usa el JDK/SDK recomendados por Android Studio.

Run ▶️
Emulador o dispositivo (SDK 24+).





## 🧩 Módulos principales

### 🔐 Autenticación y sesión
- Persistencia con **DataStore**.
- Navegación protegida por **rol**.
- Manejo de **errores** visible en UI.

### 🛒 Carrito
- Incremento/decremento de unidades y eliminación automática a **0**.
- **Subtotales** y **total** en tiempo real.

### 📦 Productos
- Listado, detalle y variantes (**color/talla**).
- **Stock** y **precio** dinámico por variante.

### 🧑‍💼 Admin
- **CRUD** de categorías/productos.
- Base de **Reportes** lista para integración.


## 👤 Créditos

**StoreFit — Grupo 1**  
Benjamín Palma y Gustavo Espinoza

> Para consultas, contacta a los autores. **Repositorio de solo lectura**: no acepta cambios externos.



