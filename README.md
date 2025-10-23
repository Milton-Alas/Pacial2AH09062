# 📱 PedidosApp - Aplicación Android con MBaaS

> **Proyecto Académico - Parcial 2 AH09062**  
> **Autor:** Milton Alas  
> **Fecha:** Octubre 2025

## 📋 Descripción

Aplicación móvil Android desarrollada en **Java** que implementa un sistema de autenticación y gestión de usuarios con **arquitectura offline-first**, integración con **Firebase (MBaaS)** y base de datos local **SQLite/Room**.

## ✨ Características Principales

### 🔐 **Sistema de Autenticación**
- **Splash Screen** con redirección inteligente
- **Login** con validación de credenciales
- **Registro de usuarios** con validaciones
- **"Recordar usuario"** usando SharedPreferences
- **Gestión de sesiones** persistentes

### 💾 **Gestión de Datos**
- **Base de datos local** con Room (SQLite)
- **Sincronización con Firebase Firestore** (MBaaS)
- **Modo offline-first** - funciona sin conexión
- **Sincronización automática** cuando hay conectividad
- **Estado de sincronización** visible para el usuario

### 👤 **Gestión de Perfil**
- **Actualización de perfil** de usuario
- **Validaciones en tiempo real**
- **Sincronización bidireccional** (local ↔ Firebase)
- **Manejo de estados pendientes** de sincronización

### 🗺️ **Integración con Mapas**
- Preparado para **Google Maps** integration
- Ubicación de negocio/comercio relacionado al proyecto

## 🏗️ Arquitectura Técnica

### **Patrón Repository**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Activities    │───▶│  UserRepository │───▶│  Room (SQLite)  │
│  (UI Layer)     │    │ (Business Logic)│    │  (Local Cache)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                        ┌─────────────────┐
                        │Firebase Firestore│
                        │  (Cloud MBaaS)  │
                        └─────────────────┘
```

### **Componentes Clave**
- **Room Database**: Base de datos local con DAO pattern
- **Firebase Manager**: Wrapper para operaciones de Firestore
- **User Repository**: Única fuente de verdad para datos de usuario
- **Preference Manager**: Gestión de SharedPreferences
- **Validation Utils**: Validaciones centralizadas

## 📱 Capturas de Pantalla

### Flujo Principal
```
Splash Screen → Login → Home → Profile
      ↓           ↓       ↓        ↓
   2 segundos   Validación  Sync   Edición
   de carga     credenciales Status perfil
```

## 🛠️ Tecnologías Utilizadas

### **Frontend**
- **Java** - Lenguaje principal
- **Android SDK** (API 24-34)
- **Material Components** - UI/UX moderna
- **ViewBinding** - Referencias de vistas seguras

### **Base de Datos**
- **Room** - ORM para SQLite local
- **Firebase Firestore** - Base de datos NoSQL en la nube
- **SharedPreferences** - Almacenamiento de preferencias

### **Arquitectura**
- **Repository Pattern** - Separación de capas
- **Singleton Pattern** - Instancias únicas
- **Observer Pattern** - Callbacks asíncronos
- **MVVM aproximado** - Separación UI/Lógica

## 📋 Validaciones Implementadas

| Campo | Validación | Mensaje de Error |
|-------|------------|------------------|
| **Email** | Formato válido | "Por favor ingrese un email válido" |
| **Contraseña** | ≥8 caracteres | "La contraseña debe tener al menos 8 caracteres" |
| **Nombre** | ≥7 caracteres | "El nombre completo debe tener al menos 7 caracteres" |

## 🚀 Instalación y Configuración

### **1. Requisitos Previos**
- Android Studio Arctic Fox o superior
- Java 11+
- SDK Android 24+ (Target: 34)
- Cuenta de Firebase (opcional)

### **2. Clonar el Repositorio**
```bash
git clone https://github.com/tu-usuario/Pacial2AH09062.git
cd Pacial2AH09062
```

### **3. Configuración de Firebase (Opcional)**
1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Crear proyecto nuevo
3. Agregar aplicación Android:
   - **Package Name**: `com.example.pacial2ah09062`
4. Descargar `google-services.json`
5. Colocar en `app/google-services.json`
6. Descomentar código Firebase en `UserRepository.java`

### **4. Compilar y Ejecutar**
```bash
./gradlew assembleDebug
# O abrir en Android Studio y ejecutar
```

## 👤 Usuario de Prueba

Para facilitar las pruebas, se crea automáticamente un usuario:

- **Email**: `admin@test.com`
- **Contraseña**: `admin123`
- **Nombre**: `Usuario Administrador`

## 📂 Estructura del Proyecto

```
app/src/main/java/com/example/pacial2ah09062/
├── activities/
│   ├── SplashActivity.java     # Pantalla de inicio
│   ├── LoginActivity.java      # Autenticación
│   ├── RegisterActivity.java   # Registro de usuarios
│   ├── HomeActivity.java       # Pantalla principal
│   ├── ProfileActivity.java    # Gestión de perfil
│   └── MainActivity.java       # Testing y pruebas
├── database/
│   ├── User.java              # Entidad de usuario
│   ├── UserDAO.java           # Data Access Object
│   └── AppDatabase.java       # Configuración Room
├── firebase/
│   └── FirebaseManager.java   # Wrapper Firebase
├── repository/
│   └── UserRepository.java    # Patrón Repository
└── utils/
    ├── ValidationUtils.java   # Validaciones
    └── PreferenceManager.java # SharedPreferences
```

## 🔄 Flujo de Sincronización

### **Modo Offline-First**
```
1. 💾 Operación → SQLite (Local)
2. 🔄 Marcar como pendingSync = true
3. 🌐 Intentar sync con Firebase
4. ✅ Si éxito → pendingSync = false
5. ❌ Si falla → Mantener local, sync después
```

### **Estados de Sincronización**
- ✅ **Sincronizado**: Datos iguales en local y Firebase
- ⏳ **Pendiente**: Cambios locales sin sincronizar
- ❌ **Error**: Falló la sincronización, se reintentará

## 📊 Funcionalidades por Activity

### **SplashActivity**
- ✅ Logo de la aplicación
- ✅ Progreso de carga (2 segundos)
- ✅ Redirección inteligente según estado de sesión

### **LoginActivity**
- ✅ Validación en tiempo real
- ✅ "Recordar usuario" con checkbox
- ✅ Navegación a registro
- ✅ Gestión de estados de carga

### **RegisterActivity**
- ✅ Validación de campos
- ✅ Registro simultáneo (local + Firebase)
- ✅ Feedback visual de estados
- ✅ Navegación de retorno

### **HomeActivity**
- ✅ Toolbar personalizada
- ✅ Información del usuario
- ✅ Estado de sincronización
- ✅ Navegación a perfil y mapa
- ✅ Menú con logout

### **ProfileActivity**
- ✅ Edición de nombre de usuario
- ✅ Validación en tiempo real
- ✅ Actualización local + Firebase
- ✅ Advertencias de sincronización
- ✅ Botón "Reintentar Sync"

## 🧪 Testing

La aplicación incluye testing automático en `MainActivity.java`:

```java
// Pruebas automáticas:
✅ Registro de usuario
✅ Autenticación
✅ Actualización de perfil
✅ Sincronización de datos
```

## 📈 Características Avanzadas

### **Manejo de Errores**
- Validación exhaustiva de inputs
- Mensajes de error contextuales
- Recuperación automática de fallos
- Logs detallados para debugging

### **UX/UI**
- Material Design 3.0
- Animaciones y transiciones
- Estados de carga visuales
- Feedback inmediato al usuario

### **Performance**
- Operaciones asíncronas con ExecutorService
- Caché local con Room
- Singleton pattern para repositories
- Lazy loading de datos

## 🔮 Próximas Funcionalidades

- [ ] **Google Maps** integration completa
- [ ] **Push notifications** con Firebase
- [ ] **Modo oscuro** automático
- [ ] **Biometric authentication**
- [ ] **Export/Import** de datos
- [ ] **Multi-idioma** (i18n)

## 🤝 Contribución

Este es un proyecto académico, pero sugerencias y mejoras son bienvenidas:

1. Fork del repositorio
2. Crear branch: `git checkout -b feature/nueva-funcionalidad`
3. Commit: `git commit -m 'Agregar nueva funcionalidad'`
4. Push: `git push origin feature/nueva-funcionalidad`
5. Pull Request

## 📄 Licencia

Proyecto académico bajo licencia MIT. Ver `LICENSE` para más detalles.

## 👨‍💻 Autor

**Milton Alas**
- 📧 Email: [ah09062@ues.edu.sv](mailto:ah09062@ues.edu.sv)
- 🎓 Universidad: Universidad de El Salvador
- 📚 Curso: Desarrollo de Aplicaciones Móviles II
- 📅 Parcial 2 - AH09062

---

### 🏆 Estado del Proyecto

```
✅ Splash Screen
✅ Sistema de Login/Registro
✅ Base de datos local (Room)
✅ Integración Firebase (MBaaS)
✅ Gestión de Perfil
✅ Sincronización offline-first
✅ Validaciones completas
✅ UI/UX Material Design
⏳ Google Maps (preparado)
⏳ Funcionalidades adicionales
```

**Última actualización:** Octubre 2025

---

*⭐ Si este proyecto te fue útil, no olvides darle una estrella!*
