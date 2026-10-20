# 🛒 Lista de la compra compartida — Guía paso a paso desde cero

> Proyecto de 2.º DAM · Android nativo (Kotlin + Jetpack Compose) · Firebase · GitHub

## 0. Estado del proyecto (actualizado)

Revisado en `C:\Users\lolpj\AndroidStudioProjects\Listadelacompra`:

| Elemento | Estado |
|---|---|
| Proyecto Android Studio | ✅ Creado (plantilla Compose con *Navigation Suite*) |
| Paquete / applicationId | `org.insbaixcamp.listadelacompra` |
| Versiones | AGP 9.4.1 · Kotlin 2.2.10 · compileSdk/targetSdk 37 · minSdk 24 · Compose BoM 2025.12.00 |
| Repositorio Git local | ✅ Creado (ramas `main` y `develop`) |
| Repositorio en GitHub | ✅ `https://github.com/Joel-lopez2/lista-compra-compartida` |
| Firebase | ✅ Conectado (proyecto `lista-compra-compartida-1b43d`, Auth e-mail/contraseña y Firestore `eur3` activados) |
| Código de la app | Modelo de datos creado; resto pendiente |

### Progreso

| Fase | Estado |
|---|---|
| 1. Git y GitHub | ✅ Completada |
| 2. Firebase | ✅ Completada (SHA-1 pospuesto: no hace falta para e-mail/contraseña) |
| 3. Diseño y modelo de datos | ✅ Completada (diseño en `docs/DISENO.md`, tema verde aplicado) |
| 4. Autenticación | ✅ Completada y probada (merge a `develop`) |
| 5. Gestión de listas | ✅ Completada y probada (merge a `develop`) |
| 6. Categorías y productos | ✅ Completada (merge a `develop`) |
| 7. Compartir listas | 🔄 En curso (decisiones tomadas, rama `feature/compartir`) |
| 8. Característica extra | ⏳ Pendiente |
| 9. Reglas de seguridad | ⏳ Pendiente |
| 10. Pulido y pruebas | ⏳ Pendiente |
| 11. APK y entrega | ⏳ Pendiente |

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

## 2. FASE 1 — Git y GitHub (repositorio desde cero) ✅

### 2.1 Preparar Git en el ordenador
- [x] Instalar Git para Windows (si no está): <https://git-scm.com>
- [x] Comprobar en una terminal: `git --version`
- [x] Configurar identidad (una sola vez):
  ```
  git config --global user.name "Tu Nombre"
  git config --global user.email "tu_correo@gmail.com"
  ```

### 2.2 Crear el repositorio en GitHub
- [x] Entrar en <https://github.com> y crear cuenta si no tienes
- [x] **New repository** → nombre: `lista-compra-compartida`
- [x] Visibilidad: **Public** (más fácil para el profesorado) o Private invitando al profesor como colaborador
- [x] **No** marcar "Add README" ni `.gitignore` (el proyecto ya tiene `.gitignore` y evitamos conflictos en el primer push)
- [x] Copiar la URL del repositorio

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
- [x] Verificar que en GitHub aparece el código
- [x] **Comprobar que NO se ha subido `local.properties`** (contiene rutas locales; debe estar en `.gitignore`)

### 2.4 Flujo de ramas (sencillo)
- [x] Crear la rama de desarrollo: `git checkout -b develop`
- [x] Una rama por funcionalidad: `feature/auth`, `feature/listas`, `feature/productos`, `feature/compartir`, `feature/extra`
- [x] Al terminar cada una: merge a `develop`; al cerrar versión: merge a `main`
- [x] Mensajes de commit claros (ej. `Añade pantalla de login con Firebase Auth`)
- [x] Hacer commits pequeños y frecuentes (el historial demuestra el trabajo continuo)

> Alternativa gráfica: Android Studio → *Git → Create Git Repository* y *Git → GitHub → Share Project on GitHub*.

---

## 3. FASE 2 — Crear y conectar Firebase ✅

### 3.1 Crear el proyecto en Firebase
- [x] Entrar en <https://console.firebase.google.com> con tu cuenta de Google
- [x] **Add project** → nombre: `lista-compra-compartida`
- [x] Google Analytics: se puede desactivar (no hace falta)

### 3.2 Registrar la app Android
- [x] En el proyecto → **Add app → Android**
- [x] **Package name:** `org.insbaixcamp.listadelacompra` (debe coincidir exactamente con `applicationId`)
- [x] Nombre: Lista de la compra
- [ ] *(Pospuesto, no necesario para e-mail/contraseña; solo para Google/teléfono)* **SHA-1 de depuración**: desde la carpeta del proyecto
  ```
  gradlew.bat signingReport
  ```
  Copiar el `SHA-1` de la variante `debug` y pegarlo en *Configuración del proyecto → Tus apps → Añadir huella digital*
- [x] Descargar `google-services.json` y colocarlo en `app/` (junto a `app/build.gradle.kts`)
- [x] ⚠️ Decidir si `google-services.json` se sube a GitHub. Para un proyecto de clase es habitual subirlo; si el repositorio es público, no incluir claves privadas y proteger los datos con reglas de Firestore

### 3.3 Activar los servicios
- [x] **Authentication → Sign-in method → Correo electrónico/contraseña** → activar
- [x] **Firestore Database → Crear base de datos**
  - Modo: producción (reglas cerradas, las definimos en la Fase 9)
  - Ubicación: `eur3 (europe-west)` o la más cercana
- [x] (Más adelante) **App Distribution** para repartir el APK

### 3.4 Añadir Firebase a Gradle
Editar los archivos del proyecto (usar el catálogo `gradle/libs.versions.toml`, como ya hace la plantilla):

**`gradle/libs.versions.toml`** — añadir:
```toml
[versions]
googleServices = "4.5.0"
firebaseBom = "34.19.0"
navigationCompose = "2.10.2"
lifecycleViewmodelCompose = "2.11.0"
coroutinesPlayServices = "1.10.2"

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
- [x] **Sync Gradle** y comprobar que compila
- [x] Añadir permiso de Internet en `AndroidManifest.xml`: `<uses-permission android:name="android.permission.INTERNET" />`
- [x] Ejecutar la app en el emulador/móvil y comprobar que no hay errores de Firebase en Logcat
- [x] Commit: `Conecta Firebase (Auth y Firestore)` y push

> Versiones aplicadas y comprobadas en octubre de 2026. El proyecto compila y arranca con ellas (AGP 9.4.1).
> Pendiente de commit en la rama `feature/firebase`.

---

## 4. FASE 3 — Diseño y modelo de datos ✅

### 4.1 Bocetos (papel o Figma)
- [x] Login / Registro
- [x] Mis listas (propias + compartidas)
- [x] Detalle de lista (productos por categoría + completados)
- [x] Diálogo compartir / ver miembros
- [x] Estilo minimalista inspirado en Recordatorios de iOS (paleta verde aplicada en `ui/theme`)

> Los bocetos, el mapa de navegación y la paleta están en **`docs/DISENO.md`**.

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
- [x] Crear las `data class` del modelo en `data/model` (`User`, `ShoppingList`, `Category`, `Product`), con valores por defecto y `@DocumentId`, necesarios para que Firestore las deserialice
- [x] El resto de paquetes (`repository`, `ui`, `navigation`, `util`) se crean junto con el primer archivo de cada uno, a partir de la Fase 4
- [ ] Commit: `Añade modelo de datos y diseño de pantallas`

> La plantilla trae `ic_home`, `ic_favorite` y `ic_account_box` y un `NavigationSuiteScaffold` de ejemplo en `MainActivity`. Se sustituirá por nuestra navegación.

---

## 5. FASE 4 — Autenticación (`feature/auth`) ✅

Rama: `feature/auth` (creada desde `develop`). Decisiones tomadas punto por punto:

| # | Punto | Decisión |
|---|---|---|
| 1 | `AuthRepository` | Clase en `data/repository` con `FirebaseAuth` y `FirebaseFirestore`: `register`, `login`, `logout`, `currentUser`. Funciones `suspend` con `await()` (coroutines-play-services) |
| 2 | Documento `users/{uid}` | Al registrarse se crea con `uid`, `email` y `displayName`. Si falla el guardado, se informa del error |
| 3 | `AuthViewModel` | Un único `AuthUiState` (cargando, error, éxito) expuesto en un `StateFlow`. El ViewModel crea `AuthRepository()` por defecto, sin Hilt ni factory |
| 4 | `LoginScreen` | Correo + contraseña (con mostrar/ocultar), botón y enlace a "Regístrate" |
| 5 | `RegisterScreen` | **Nombre + correo + contraseña + repetir contraseña** |
| 6 | Validaciones | Campos no vacíos, correo con formato válido, contraseña ≥ 6, las dos contraseñas coinciden. Se validan antes de llamar a Firebase |
| 7 | Mensajes en español | Todos en `strings.xml`; los errores de Firebase (credenciales, correo ya usado, sin red) se traducen a mensajes propios |
| 8 | Navegación | Navigation Compose: arranca en `login` si no hay sesión y en `lists` si la hay. Se sustituye el `NavigationSuiteScaffold` de la plantilla |
| 9 | Cerrar sesión | **Icono en la barra superior de "Mis listas"**, con diálogo de confirmación |
| 10 | Recuperar contraseña | ❌ No de momento (no es requisito; opcional al final) |
| 11 | Pruebas | Registro, login, error por contraseña incorrecta, sesión persistente tras cerrar la app, cierre de sesión |
| 12 | Merge | `feature/auth` → `develop` |

Tareas:
- [x] `AuthRepository`
- [x] Creación de `users/{uid}` al registrarse
- [x] `AuthViewModel` + `AuthUiState`
- [x] `LoginScreen` y `RegisterScreen`
- [x] Validaciones y mensajes en `strings.xml`
- [x] Navegación según sesión (`Routes`, `NavGraph`) y `MainActivity` actualizado
- [x] Pantalla provisional "Mis listas" con el botón de cerrar sesión
- [x] Probar los casos del punto 11 (registro, login, errores, sesión persistente y cierre de sesión: todo correcto)
- [x] Merge a `develop`

---

## 6. FASE 5 — Gestión de listas (`feature/listas`) ✅

Rama: `feature/listas` (creada desde `develop`). Decisiones tomadas punto por punto:

| # | Punto | Decisión |
|---|---|---|
| 1 | `ListRepository` | En `data/repository`. Escucha en tiempo real `lists.whereArrayContains("members", uid)` con un `callbackFlow`. Funciones `suspend`: crear, renombrar, eliminar |
| 2 | Orden | **Por fecha de creación, las más recientes arriba.** Se ordena en la app (no en la consulta) para no necesitar un índice compuesto de Firestore. Una lista recién creada, con `createdAt` aún sin confirmar, se trata como la más nueva |
| 3 | `ListsViewModel` | `ListsUiState` único (cargando, error, listas propias, compartidas) en un `StateFlow`; el ViewModel crea `ListRepository()` por defecto |
| 4 | `ListsScreen` | Secciones **Mis listas** y **Compartidas conmigo** (según `ownerId == uid`); botón flotante (+); icono de cerrar sesión de la Fase 4 |
| 5 | Crear lista | Diálogo con nombre obligatorio (sin espacios sobrantes, máximo 40 caracteres). `ownerId` = usuario actual y `members` = [su uid] |
| 6 | Renombrar y eliminar | Menú ⋮ solo en las listas propias: *Renombrar* y *Eliminar*. Eliminar pide confirmación. Las listas compartidas no tienen menú hasta la Fase 7 (*Salir de la lista*) |
| 7 | **Borrado en cascada** | **Desde la app, con un `WriteBatch`**: se leen `categories` y `products` de la lista y se borran junto con la lista en una sola operación atómica (límite de 500 documentos, de sobra). Sin Cloud Functions |
| 8 | **Contador de productos** | **Sí, ahora.** Cada tarjeta muestra "N productos" con un listener en tiempo real sobre `lists/{id}/products` (0 hasta la Fase 6) |
| 9 | **Propietario en compartidas** | **Sí.** En las compartidas se muestra "de {nombre}" leyendo `users/{ownerId}` una sola vez y guardándolo en memoria |
| 10 | Estados | Cargando (hasta la primera respuesta), vacío ("Aún no tienes listas…") y error (mensaje con botón *Reintentar*) |
| 11 | Navegación | Pulsar una tarjeta navega a `detail/{listId}`. Hasta la Fase 6 es una pantalla provisional con el nombre de la lista |
| 12 | Textos | Todos en `strings.xml` |
| 13 | Pruebas | Crear, renombrar, eliminar (comprobando en Firestore que se borra la lista y sus subcolecciones), lista vacía, orden, contador, sin red |
| 14 | Merge | `feature/listas` → `develop` |

Tareas:
- [x] `ListRepository` (escuchar, crear, renombrar, eliminar con batch)
- [x] `ListsViewModel` + `ListsUiState` (con contador y nombre del propietario)
- [x] `ListsScreen` con secciones, tarjetas, FAB y estados
- [x] Diálogos de crear, renombrar y confirmar eliminación
- [x] Ruta `detail/{listId}` con pantalla provisional
- [x] Textos en `strings.xml`
- [x] Probar los casos del punto 13 (crear, renombrar, eliminar con borrado en cascada, lista vacía, orden, contador y detalle provisional: todo correcto)
- [x] Merge a `develop`

---

## 7. FASE 6 — Categorías y productos (`feature/productos`) ✅

Rama: `feature/productos` (creada desde `develop`, que ya incluye `feature/listas`). Decisiones tomadas punto por punto:

| # | Punto | Decisión |
|---|---|---|
| 1 | Repositorios | `CategoryRepository` y `ProductRepository` en `data/repository`, con `callbackFlow` en tiempo real sobre `lists/{listId}/categories` y `lists/{listId}/products`. Funciones `suspend` para crear, editar y eliminar |
| 2 | ViewModel | `ListDetailViewModel` con un único `DetailUiState` (cargando, error, categorías, productos agrupados, completados). Recibe el `listId` por la ruta y también escucha el documento de la lista para mostrar su nombre actualizado |
| 3 | Lista eliminada mientras está abierta | Si el propietario la borra, el detalle detecta que ya no existe y vuelve a "Mis listas" |
| 4 | `ListDetailScreen` | Productos agrupados por categoría (cabecera con icono y nombre) y al final "Sin categoría". Sustituye la pantalla provisional |
| 5 | **Añadir productos** | **Barra inferior de añadido rápido**: se escribe el nombre y se añade al instante (nombre obligatorio, sin espacios sobrantes). Entra en "Sin categoría"; en la Fase 8 se autoclasificará |
| 6 | Editar producto | Al tocar un producto se abre un diálogo con nombre (obligatorio), cantidad (opcional, texto libre) y categoría (**campo desplegable con buscador**: al pulsarlo se abre una ventana con buscador, sin tildes ni mayúsculas, y la lista de categorías, incluida "Sin categoría") |
| 7 | **Eliminar producto** | **Botón "Eliminar" en el diálogo de edición, con confirmación** |
| 8 | **Categorías** | **Menú ⋮ en cada cabecera** (Renombrar / Eliminar) **y "Nueva categoría" en el menú superior**. Nombre obligatorio e icono (emoji) opcional |
| 9 | Eliminar categoría | Confirmación; sus productos pasan a "Sin categoría" (se actualizan en un batch antes de borrar la categoría) |
| 10 | **Completados** | **Se quedan en su categoría**, marcados con la casilla y con el nombre tachado y en gris. No se mueven a ninguna sección aparte: dentro de su categoría **bajan al final** (primero los pendientes y luego los completados, cada grupo en orden de creación). Al desmarcarlos vuelven a su posición. **Nunca se eliminan al completarlos** |
| 11 | Permisos | Cualquier miembro de la lista puede ver, crear, editar y eliminar categorías y productos (ya lo permiten las reglas de Firestore) |
| 12 | Estados | Cargando, lista vacía ("Añade tu primer producto") y error con *Reintentar* |
| 13 | Textos | Todos en `strings.xml` |
| 14 | Pruebas | Con un solo usuario: crear, editar y eliminar productos y categorías; completar y desmarcar; eliminar una categoría con productos; el contador de la tarjeta en "Mis listas"; eliminar la lista con contenido |
| 15 | Merge | `feature/productos` → `develop` |

Tareas:
- [x] `CategoryRepository` y `ProductRepository` con Flow en tiempo real
- [x] `ListDetailViewModel` + `DetailUiState` (agrupación por categoría)
- [x] `ListDetailScreen`: productos por categoría, con los completados tachados al final de su categoría
- [x] Barra inferior de añadido rápido
- [x] Diálogo de producto (editar, cantidad, categoría, eliminar con confirmación)
- [x] CRUD de categorías (menú ⋮, nueva categoría, eliminar con productos a "Sin categoría")
- [x] Marcar/desmarcar completado
- [x] Textos en `strings.xml`
- [ ] Probar los casos del punto 14 *(código escrito; pendiente de compilar y probar)*
- [x] Merge a `develop`

---

## 8. FASE 7 — Compartir listas (`feature/compartir`) 🔄

Rama: `feature/compartir` (creada desde `develop`, que ya incluye `feature/productos`). Decisiones tomadas punto por punto:

| # | Punto | Decisión |
|---|---|---|
| 1 | **Permisos** | **Solo el propietario** invita y quita miembros. Los invitados pueden ver y editar categorías y productos, ver quién tiene acceso y **salir de la lista**. El propietario no puede salir (tiene que eliminarla) |
| 2 | **Cómo se invita** | **Por correo electrónico.** Se busca en `users` por el campo `email`. La persona tiene que estar registrada |
| 3 | Correos en minúsculas | El correo se guarda y se busca siempre en minúsculas y sin espacios. Se ajusta `AuthRepository.register` para guardarlo así (los usuarios de prueba creados con mayúsculas habrá que registrarlos de nuevo) |
| 4 | `ShareDialog` | Campo de correo + botón *Compartir*, y debajo la lista de miembros. Mensajes: "Escribe un correo válido", "Ese usuario no está registrado", "Ya tiene acceso a esta lista", "No puedes compartir una lista contigo mismo" |
| 5 | Añadir y quitar | `members` se modifica con `arrayUnion` / `arrayRemove` (sin duplicados). Se añade cuando la búsqueda encuentra al usuario |
| 6 | Miembros | Se muestran nombre y correo de cada miembro (leyendo `users/{uid}`), con el propietario marcado como "Propietario". El propietario tiene un botón de quitar en los demás, con confirmación |
| 7 | **Salir de la lista** | Opción en el menú ⋮ de la tarjeta (listas compartidas) y en el diálogo de miembros, con confirmación. La lista desaparece de "Compartidas conmigo" |
| 8 | **Dónde está "Compartir"** | **Menú ⋮ de la tarjeta (propietario) y menú superior del detalle.** En el detalle, todos los miembros ven *Miembros*; solo el propietario ve añadir y quitar |
| 9 | Si te quitan de la lista | Si el propietario te quita (o borra la lista) mientras la tienes abierta, la app vuelve a "Mis listas" |
| 10 | **Reglas de Firestore** | **Se afinan ahora** (ver abajo): solo el propietario modifica `members`; un miembro solo puede quitarse a sí mismo |
| 11 | Pruebas | Con dos cuentas: invitar, comprobar que aparece en "Compartidas conmigo" con el nombre del propietario, que el invitado edita productos y categorías, que se ve en tiempo real (dos dispositivos), quitar a un miembro, salir de la lista y los mensajes de error |
| 12 | Merge | `feature/compartir` → `develop` |

### Reglas de Firestore (para pegar en la consola)

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    match /users/{uid} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == uid;
    }

    match /lists/{listId} {
      // Crear: el creador es el propietario y el unico miembro inicial
      allow create: if request.auth != null
                    && request.resource.data.ownerId == request.auth.uid
                    && request.resource.data.members == [request.auth.uid];

      // Leer: cualquier miembro
      allow read: if request.auth.uid in resource.data.members;

      // Modificar: el propietario todo; un miembro solo puede quitarse a si mismo
      allow update: if request.auth.uid == resource.data.ownerId
                    || (request.auth.uid in resource.data.members
                        && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['members'])
                        && request.resource.data.members == resource.data.members.removeAll([request.auth.uid]));

      // Eliminar: solo el propietario
      allow delete: if request.auth.uid == resource.data.ownerId;

      // Categorias y productos: cualquier miembro
      match /{sub=**} {
        allow read, write: if request.auth.uid in
          get(/databases/$(database)/documents/lists/$(listId)).data.members;
      }
    }
  }
}
```

Tareas:
- [x] Guardar el correo en minúsculas al registrarse
- [x] `ShareRepository` (o ampliar `ListRepository`): buscar usuario por correo, añadir, quitar, salir y leer miembros
- [x] `ShareViewModel` + estado (buscando, error, miembros)
- [x] `ShareDialog` con correo y lista de miembros
- [x] Salir de la lista (tarjeta compartida y diálogo de miembros)
- [x] Menú ⋮ de la tarjeta (Compartir) y menú superior del detalle (Compartir / Miembros)
- [x] Volver a "Mis listas" si pierdes el acceso
- [x] Textos en `strings.xml`
- [ ] Publicar las reglas nuevas en la consola de Firebase
- [ ] Probar los casos del punto 11 *(código escrito; pendiente de publicar las reglas, compilar y probar)*
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
