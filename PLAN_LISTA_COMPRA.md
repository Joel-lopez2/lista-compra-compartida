# 🛒 Lista de la compra compartida — Guía paso a paso desde cero

> Proyecto de 2.º DAM · Android nativo (Kotlin + Jetpack Compose) · Firebase · GitHub

## 0. Punto de partida (estado real del proyecto)

Revisado en `C:\Users\lolpj\AndroidStudioProjects\Listadelacompra`:

| Elemento | Estado |
|---|---|
| Proyecto Android Studio | ✅ Creado (plantilla Compose con *Navigation Suite*) |
| Paquete / applicationId | `org.insbaixcamp.listadelacompra` |
| Versiones | AGP 9.4.1 · Kotlin 2.2.10 · compileSdk/targetSdk 37 · minSdk 24 · Compose BoM 2025.12.00 |
| Repositorio Git local | ❌ No existe (no hay carpeta `.git`) |
| Repositorio en GitHub | ❌ No creado |
| Firebase | ❌ No conectado (sin `google-services.json`, sin plugin, sin dependencias) |
| Código de la app | Solo `MainActivity` y tema de la plantilla |

**Orden del plan:** Git/GitHub → Firebase → dependencias → modelo y arquitectura → funcionalidades → extra → seguridad → APK → entrega.

---

## 1. Objetivo y requisitos

Aplicación donde cada usuario gestiona **sus listas**, puede **compartirlas** y editar **categorías y productos**. Los productos se marcan como **completados** sin borrarse.

| Requisito del enunciado | Cómo lo cubrimos |
|---|---|
| Lista única y personal, compartible | `ownerId` + array `members`; se comparte por email |
| Varias listas por usuario (propias y compartidas) | Pantalla "Mis listas" con *Mis listas* / *Compartidas conmigo* |
| Ver, crear, editar y eliminar categorías y productos (cualquier usuario con acceso) | CRUD en subcolecciones; reglas permiten a todos los `members` |
| Completar productos sin eliminarlos | Campo `completed` (tachado) |
| Nombre mínimo en categoría y producto | Validación de campo obligatorio |
| Accesible para el profesorado | APK en GitHub Releases / Firebase App Distribution |
| Característica extra | Autoclasificación + productos predefinidos con iconos |

**Stack:** Kotlin · Jetpack Compose + Material 3 · MVVM · Navigation Compose · Firebase Authentication · Cloud Firestore · Coroutines/StateFlow · Git + GitHub.

---

## 2. FASE 1 — Git y GitHub (repositorio desde cero)

### 2.1 Preparar Git en el ordenador
- [ ] Instalar Git para Windows (si no está): <https://git-scm.com>
- [ ] Comprobar en una terminal: `git --version`
- [ ] Configurar identidad (una sola vez):
  ```
  git config --global user.name "Tu Nombre"
  git config --global user.email "tu_correo@gmail.com"
  ```

### 2.2 Crear el repositorio en GitHub
- [ ] Entrar en <https://github.com> y crear cuenta si no tienes
- [ ] **New repository** → nombre: `lista-compra-compartida`
- [ ] Visibilidad: **Public** (más fácil para el profesorado) o Private invitando al profesor como colaborador
- [ ] **No** marcar "Add README" ni `.gitignore` (el proyecto ya tiene `.gitignore` y evitamos conflictos en el primer push)
- [ ] Copiar la URL del repositorio

### 2.3 Inicializar y subir el proyecto
Desde la carpeta del proyecto (`Listadelacompra`):
```
git init
git branch -M main
git add .
git commit -m "Proyecto inicial de Android Studio"
git remote add origin https://github.com/TU_USUARIO/lista-compra-compartida.git
git push -u origin main
```
- [ ] Verificar que en GitHub aparece el código
- [ ] **Comprobar que NO se ha subido `local.properties`** (contiene rutas locales; debe estar en `.gitignore`)

### 2.4 Flujo de ramas (sencillo)
- [ ] Crear la rama de desarrollo: `git checkout -b develop`
- [ ] Una rama por funcionalidad: `feature/auth`, `feature/listas`, `feature/productos`, `feature/compartir`, `feature/extra`
- [ ] Al terminar cada una: merge a `develop`; al cerrar versión: merge a `main`
- [ ] Mensajes de commit claros (ej. `Añade pantalla de login con Firebase Auth`)
- [ ] Hacer commits pequeños y frecuentes (el historial demuestra el trabajo continuo)

> Alternativa gráfica: Android Studio → *Git → Create Git Repository* y *Git → GitHub → Share Project on GitHub*.

---

## 3. FASE 2 — Crear y conectar Firebase

### 3.1 Crear el proyecto en Firebase
- [ ] Entrar en <https://console.firebase.google.com> con tu cuenta de Google
- [ ] **Add project** → nombre: `lista-compra-compartida`
- [ ] Google Analytics: se puede desactivar (no hace falta)

### 3.2 Registrar la app Android
- [ ] En el proyecto → **Add app → Android**
- [ ] **Package name:** `org.insbaixcamp.listadelacompra` (debe coincidir exactamente con `applicationId`)
- [ ] Nombre: Lista de la compra
- [ ] **SHA-1 de depuración** (necesario para Auth): desde la carpeta del proyecto
  ```
  gradlew.bat signingReport
  ```
  Copiar el `SHA-1` de la variante `debug` y pegarlo en *Configuración del proyecto → Tus apps → Añadir huella digital*
- [ ] Descargar `google-services.json` y colocarlo en `app/` (junto a `app/build.gradle.kts`)
- [ ] ⚠️ Decidir si `google-services.json` se sube a GitHub. Para un proyecto de clase es habitual subirlo; si el repositorio es público, no incluir claves privadas y proteger los datos con reglas de Firestore

### 3.3 Activar los servicios
- [ ] **Authentication → Sign-in method → Correo electrónico/contraseña** → activar
- [ ] **Firestore Database → Crear base de datos**
  - Modo: producción (reglas cerradas, las definimos en la Fase 9)
  - Ubicación: `eur3 (europe-west)` o la más cercana
- [ ] (Más adelante) **App Distribution** para repartir el APK

### 3.4 Añadir Firebase a Gradle
Editar los archivos del proyecto (usar el catálogo `gradle/libs.versions.toml`, como ya hace la plantilla):

**`gradle/libs.versions.toml`** — añadir:
```toml
[versions]
googleServices = "4.4.x"        # comprobar la última versión estable
firebaseBom = "34.x.x"          # comprobar la última versión estable
navigationCompose = "2.9.x"     # comprobar la última versión estable
lifecycleViewmodelCompose = "2.11.0"
coroutinesPlayServices = "1.10.x"

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutinesPlayServices" }

[plugins]
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

**`build.gradle.kts` (raíz)** — añadir en `plugins`:
```kotlin
alias(libs.plugins.google.services) apply false
```

**`app/build.gradle.kts`** — añadir:
```kotlin
plugins {
    // ...los existentes
    alias(libs.plugins.google.services)
}

dependencies {
    // ...los existentes
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.play.services)
}
```
- [ ] **Sync Gradle** y comprobar que compila
- [ ] Añadir permiso de Internet en `AndroidManifest.xml`: `<uses-permission android:name="android.permission.INTERNET" />`
- [ ] Ejecutar la app en el emulador/móvil y comprobar que no hay errores de Firebase en Logcat
- [ ] Commit: `Conecta Firebase (Auth y Firestore)` y push

> Nota: las versiones exactas hay que consultarlas al implementarlo (Android Studio puede ayudar con *Tools → Firebase*). El proyecto usa AGP 9.x, así que si algún plugin da error de compatibilidad se revisa en ese momento.

---

## 4. FASE 3 — Diseño y modelo de datos

### 4.1 Bocetos (papel o Figma)
- [ ] Login / Registro
- [ ] Mis listas (propias + compartidas)
- [ ] Detalle de lista (productos por categoría + completados)
- [ ] Diálogo compartir / ver miembros
- [ ] Estilo minimalista inspirado en Recordatorios de iOS

### 4.2 Modelo en Firestore
```
users/{uid}
    ├─ email: String
    └─ displayName: String

lists/{listId}
    ├─ name: String
    ├─ ownerId: String
    ├─ members: List<String>        // uids con acceso (incluye propietario)
    ├─ createdAt: Timestamp
    ├─ categories/{categoryId}
    │       ├─ name: String         // obligatorio
    │       └─ icon: String         // emoji opcional
    └─ products/{productId}
            ├─ name: String         // obligatorio
            ├─ categoryId: String   // "" = sin categoría
            ├─ completed: Boolean
            ├─ quantity: String     // opcional
            ├─ tags: List<String>   // opcional
            └─ createdBy: String
```
- Mis listas y compartidas: `lists.whereArrayContains("members", uid)`; se distinguen con `ownerId == uid`
- Compartir: buscar el email en `users` y añadir su `uid` a `members`

### 4.3 Estructura de paquetes
Dentro de `org.insbaixcamp.listadelacompra`:
```
data/model        → User, ShoppingList, Category, Product
data/repository   → AuthRepository, ListRepository, CategoryRepository, ProductRepository
ui/auth           → LoginScreen, RegisterScreen, AuthViewModel
ui/lists          → ListsScreen, ListsViewModel, ShareDialog
ui/detail         → ListDetailScreen, DetailViewModel
ui/components     → diálogos, items reutilizables
ui/theme          → (ya existe)
navigation        → Routes, NavGraph
util              → AutoCategorizer, PredefinedProducts
```
- [ ] Crear los paquetes y las `data class` del modelo (con valores por defecto, necesarios para que Firestore las deserialice)
- [ ] Commit: `Añade modelo de datos y estructura de paquetes`

> La plantilla trae `ic_home`, `ic_favorite` y `ic_account_box` y un `NavigationSuiteScaffold` de ejemplo en `MainActivity`. Se sustituirá por nuestra navegación.

---

## 5. FASE 4 — Autenticación (`feature/auth`)

- [ ] `AuthRepository`: registrar, iniciar sesión, cerrar sesión, usuario actual
- [ ] Al registrarse, crear el documento `users/{uid}`
- [ ] `AuthViewModel` con `StateFlow` de estado (cargando / error / éxito)
- [ ] Pantallas `LoginScreen` y `RegisterScreen`
- [ ] Validaciones: email válido, contraseña ≥ 6 caracteres, campos no vacíos
- [ ] Mensajes de error en español
- [ ] Navegación: si hay sesión → "Mis listas"; si no → Login
- [ ] Botón de cerrar sesión
- [ ] Probar: registro, login, sesión persistente tras cerrar la app
- [ ] Merge a `develop`

---

## 6. FASE 5 — Gestión de listas (`feature/listas`)

- [ ] `ListRepository`: escuchar listas del usuario en tiempo real, crear, renombrar, eliminar
- [ ] `ListsScreen` con secciones *Mis listas* y *Compartidas conmigo*
- [ ] Crear lista (nombre obligatorio; el creador es `ownerId` y está en `members`)
- [ ] Renombrar; eliminar solo el propietario (con confirmación)
- [ ] Estados: vacío, cargando, error
- [ ] Al borrar una lista, borrar también sus categorías y productos
- [ ] Merge a `develop`

---

## 7. FASE 6 — Categorías y productos (`feature/productos`)

- [ ] `CategoryRepository` y `ProductRepository` con Flow en tiempo real
- [ ] `ListDetailScreen`: productos agrupados por categoría
- [ ] CRUD de categorías (nombre obligatorio)
- [ ] CRUD de productos (nombre obligatorio, cantidad opcional)
- [ ] Marcar/desmarcar completado (tachado, sin eliminar) y sección "Completados"
- [ ] Al eliminar una categoría: sus productos pasan a "Sin categoría"
- [ ] Probar todos los casos con un solo usuario
- [ ] Merge a `develop`

---

## 8. FASE 7 — Compartir listas (`feature/compartir`)

- [ ] `ShareDialog`: introducir email
- [ ] Buscar usuario en `users`; si no existe, mostrar "Ese usuario no está registrado"
- [ ] Añadir `uid` a `members` (evitar duplicados y compartir con uno mismo)
- [ ] Mostrar miembros de la lista; el propietario puede quitar a alguien
- [ ] Opción "Salir de la lista" para invitados
- [ ] **Prueba con dos cuentas** (emulador + móvil, o dos emuladores): ver cambios en tiempo real
- [ ] Merge a `develop`

---

## 9. FASE 8 — Característica extra (`feature/extra`)

### Extra A — Autoclasificación (principal)
- [ ] `AutoCategorizer` en `util/`: diccionario categoría → palabras clave
  - Lácteos: leche, yogur, queso, mantequilla, nata…
  - Frutas y verduras, Carnes y pescados, Panadería, Bebidas, Limpieza, Higiene…
- [ ] Normalizar texto (minúsculas, sin tildes) y buscar coincidencias
- [ ] Si la categoría existe en la lista se asigna; si no, se crea o queda "Sin categoría" (decidir y documentar)
- [ ] El usuario puede cambiarla manualmente
- [ ] Pruebas unitarias sencillas del categorizador (JUnit ya está en el proyecto)

### Extra B — Productos predefinidos con iconos (opcional)
- [ ] Catálogo de 30–40 productos habituales con emoji (🥛 Leche, 🍞 Pan, 🥚 Huevos…)
- [ ] Selector rápido al añadir producto

- [ ] Merge a `develop`

---

## 10. FASE 9 — Reglas de seguridad de Firestore

- [ ] Publicar reglas en Firestore → *Reglas*:
```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == uid;
    }
    match /lists/{listId} {
      allow create: if request.auth != null
                    && request.auth.uid == request.resource.data.ownerId;
      allow read, update: if request.auth.uid in resource.data.members;
      allow delete: if request.auth.uid == resource.data.ownerId;

      match /{sub=**} {
        allow read, write: if request.auth.uid in
          get(/databases/$(database)/documents/lists/$(listId)).data.members;
      }
    }
  }
}
```
- [ ] Probar en el simulador de reglas
- [ ] Pendiente de afinar: impedir que un miembro no propietario cambie `members` (excepto para salir él mismo)
- [ ] Ojo: la consulta `whereArrayContains("members", uid)` debe ser compatible con las reglas

---

## 11. FASE 10 — Pulido y pruebas

- [ ] Manejo de errores de red y mensajes al usuario
- [ ] Icono y nombre definitivo de la app
- [ ] Probar en emulador y en móvil real
- [ ] Tabla de casos de prueba manuales (registro, listas, compartir, completar, borrar…)
- [ ] Revisar que no hay datos sensibles en el repositorio
- [ ] Merge de `develop` a `main`

---

## 12. FASE 11 — Publicación del APK y entrega

### 12.1 Generar el APK
- [ ] Android Studio → *Build → Generate Signed App Bundle / APK → APK*
- [ ] Crear un **keystore** nuevo y **guardarlo fuera del repositorio** (con copia de seguridad; si se pierde no se puede actualizar la app)
- [ ] Añadir el **SHA-1 del keystore de release** en Firebase (si se usa Auth con firma release)
- [ ] Instalar el APK en un móvil limpio y probarlo

### 12.2 Hacerlo accesible al profesorado (elegir uno o ambos)
- [ ] **GitHub Releases:** crear tag `v1.0`, nueva *Release* y adjuntar el `.apk`
- [ ] **Firebase App Distribution:** subir el APK e invitar al profesorado por email

### 12.3 README del repositorio
- [ ] Descripción y capturas de pantalla
- [ ] Tecnologías usadas
- [ ] Enlace de descarga del APK
- [ ] Usuarios de prueba (si se quiere facilitar la corrección)
- [ ] Cómo ejecutar el proyecto (clonar, añadir `google-services.json`, ejecutar)
- [ ] Característica extra explicada

---

## 13. Planificación orientativa

| Semana | Fases | Entregable |
|---|---|---|
| 1 | 1, 2, 3 | GitHub, Firebase conectado, modelo y bocetos |
| 2 | 4 | Login y registro |
| 3 | 5 | CRUD de listas en tiempo real |
| 4 | 6 | Categorías, productos y completados |
| 5 | 7, 9 | Compartir + reglas de seguridad |
| 6 | 8 | Extra implementado |
| 7 | 10, 11 | Pruebas, APK, README |

> **Pendiente de confirmar:** fecha límite de entrega y formato exacto que pide el profesor.

---

## 14. Checklist final de entrega

- [ ] Repositorio en GitHub con historial de commits claro y README
- [ ] Firebase conectado (Auth + Firestore) y reglas publicadas
- [ ] Registro e inicio de sesión funcionan
- [ ] Cada usuario ve solo sus listas y las compartidas con él
- [ ] Varias listas por usuario, propias y compartidas, bien diferenciadas
- [ ] CRUD de categorías y productos (también para el usuario invitado)
- [ ] Completar un producto no lo elimina
- [ ] Nombre obligatorio en categorías y productos
- [ ] Compartir por email funciona y se actualiza en tiempo real
- [ ] Extra implementado y explicado
- [ ] APK accesible para el profesorado

---

## 15. Riesgos y mitigación

| Riesgo | Mitigación |
|---|---|
| `applicationId` distinto en Firebase y en Gradle | Copiar exactamente `org.insbaixcamp.listadelacompra` |
| Error de login por falta de SHA-1 | Registrar SHA-1 de debug (y de release al final) |
| Versiones incompatibles con AGP 9 / Kotlin 2.2 | Sincronizar y revisar errores al añadir cada dependencia |
| Subir `local.properties` o el keystore a GitHub | Revisar `.gitignore` antes del primer push |
| Reglas de Firestore demasiado abiertas | Probar en el simulador |
| Falta de tiempo | Prioridad: núcleo (fases 4–7) → extra A → resto |

---

## 16. Siguiente paso inmediato

1. Instalar/configurar Git y crear el repositorio en GitHub (§2)
2. Primer commit y push del proyecto actual
3. Crear el proyecto en Firebase y conectarlo (§3)
