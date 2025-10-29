# 🛍️ StoreFit — App Android (Grupo 1)

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android Studio](https://img.shields.io/badge/Android%20Studio-Giraffe%2B-3DDC84?logo=androidstudio&logoColor=white)](https://developer.android.com/studio)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-DB-1976D2)](https://developer.android.com/jetpack/androidx/releases/room)
[![CI Ready](https://img.shields.io/badge/CI-ready-success)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-black)](#-licencia)

Aplicación móvil de **e-commerce deportivo** para la marca **StoreFit**. Permite explorar productos, gestionar carrito, iniciar sesión, ver historial de compras y administrar catálogos (modo admin). Construida con **Kotlin + Jetpack Compose (Material 3)**, navegación declarativa y persistencia local con **Room/DataStore**.  

> Este repo contiene **la app Android**. El backend y microservicios viven en repos separados.

---

## 📚 Índice
- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Stack & Requisitos](#-stack--requisitos)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Configuración y ejecución](#-configuración-y-ejecución)
- [Módulos principales](#-módulos-principales)
- [Calidad & Testing](#-calidad--testing)
- [Roadmap](#-roadmap)
- [Contribuidores](#-contribuidores)
- [Agradecimientos](#-agradecimientos)
- [Licencia](#-licencia)

---

---

## ✨ Características

- **Catálogo de productos** con variantes (color/talla), stock y precio.
- **Carrito** con sumar/restar unidades, subtotal y total en tiempo real.
- **Autenticación y sesión** con **DataStore**; navegación protegida por rol.
- **Historial de compras** con tarjetas estilizadas (Material 3).
- **Panel Admin**: categorías, productos y reportes (estructura base lista).
- **UI/UX** moderna con **Jetpack Compose**, tipografías y colores coherentes.
- **Offline-first**: cache local y manejo de estados vacíos/errores.
- **Arquitectura limpia**: capas UI / Domain / Data, ViewModels y repositorios.

---

## 🧭 Arquitectura
app/
├─ ui/ # Pantallas Compose, componentes y estados UI
│ ├─ components/
│ └─ screen/
├─ navigation/ # NavHost, rutas y destino por rol
├─ data/
│ ├─ local/ # Room (DAO, Entities, Database)
│ ├─ store/ # DataStore para sesión/ajustes
│ └─ repository/ # Repositorios (fuente de verdad)
├─ domain/ # Modelos de dominio y casos de uso (si aplica)
└─ util/ # Formateadores, Result, extensiones, etc.


- **UI:** Compose + Material 3 + State Hoisting.
- **Estado:** `ViewModel` + `StateFlow`/`collectAsStateWithLifecycle`.
- **Datos:** `Repository` → `Room`/API.
- **Sesión:** `DataStore` (usuario, rol, tokens ligeros).
- **Imagenes:** `Coil`.

---

## 🧰 Stack & Requisitos

- **Kotlin** 1.9+
- **Android Studio** Giraffe/Koala o superior
- **minSdk** 24 • **targetSdk** 34 (ajústalo si tu `build.gradle` difiere)
- **Jetpack Compose** (Material 3, Navigation, Lifecycle)
- **Room**, **DataStore**, **Coil**
- (Opcional) **Hilt** para DI, **Retrofit/OkHttp** para red

---

## 🗂️ Estructura del proyecto

com.example.appstorefit_grupo1
├─ data
│ ├─ local
│ │ ├─ database/AppDatabase.kt
│ │ ├─ Productos/ProductosEntity.kt
│ │ └─ dao/.kt
│ ├─ repository/.kt
│ └─ store/SessionManager.kt
├─ navigation/.kt
├─ ui
│ ├─ components/.kt
│ └─ screen/.kt
├─ util/.kt
└─ MainActivity.kt



