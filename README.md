# 🛒 Lista de la compra compartida

Aplicación Android para gestionar listas de la compra y **compartirlas con otras personas**, que pueden verlas y editarlas en tiempo real. Proyecto de 2.º de DAM (Desarrollo de Aplicaciones Multiplataforma).

## 📲 Descarga del APK

👉 **[Descargar la última versión](https://github.com/Joel-lopez2/lista-compra-compartida/releases/latest)** (archivo `.apk` dentro de *Assets*).

Para instalarlo en un móvil Android (7.0 o superior):
1. Descarga el `.apk` desde el móvil.
2. Ábrelo. Si Android lo pide, permite **instalar aplicaciones de esta fuente**.
3. Pulsa *Instalar* y abre **Lista de la compra**.

## ✨ Funcionalidades

- **Cuenta de usuario** con correo y contraseña (Firebase Authentication). Cada persona solo ve sus listas.
- **Varias listas por usuario:** las que ha creado él ("Mis listas") y las que le han compartido ("Compartidas conmigo").
- **Categorías y productos** que cualquier miembro de la lista puede ver, crear, editar y eliminar. El nombre es obligatorio.
- **Productos completados:** se marcan como comprados y se tachan, pero no se eliminan.
- **Compartir con un código de 6 caracteres:** el propietario lo copia o lo envía por WhatsApp, correo, etc. y la otra persona pulsa *Unirse* y lo escribe. Los códigos van en mayúsculas y sin `0` ni `O` para no confundirlos.
- **Icono de lista** a elegir entre 24 emojis (o sin icono) y color propio para cada lista.
- **Progreso** "N de M comprados" con barra en cada lista.
- **Tema claro y oscuro** según el móvil.
- **Tiempo real:** los cambios de otros miembros aparecen al momento.

### ⭐ Característica extra: autoclasificación de productos

Al añadir un producto sin elegir categoría, la app lo clasifica sola: escribir **"leche"** lo mete en **"Lácteos"**. Si la categoría no existe en la lista, se crea con su emoji; si el producto no se reconoce, queda en "Sin categoría". Funciona en castellano y en catalán, ignora mayúsculas y tildes y entiende plurales sencillos. Las categorías son: Higiene, Limpieza, Lácteos, Frutas y verduras, Carnes y pescados, Panadería, Bebidas y Despensa.

Está en `util/AutoCategorizer.kt` y tiene 11 tests unitarios (`AutoCategorizerTest`).

## 🧪 Cómo probarla (para la corrección)

1. Abre la app y pulsa **Regístrate** (basta un correo inventado con formato válido, por ejemplo `profesor@ejemplo.com`, y una contraseña de 6 caracteres o más).
2. Crea una lista con **Nueva lista**, elige nombre e icono.
3. Dentro de la lista añade productos: escribe "leche" y comprueba que va a *Lácteos*. Marca productos como comprados.
4. Para probar **compartir** hacen falta dos cuentas (dos móviles, o un móvil y un emulador): en la primera pulsa *Compartir* y copia el código; en la segunda pulsa **Unirse** y escríbelo. La lista aparece en "Compartidas conmigo" y se edita en tiempo real desde ambas.

## 🧱 Tecnologías

| | |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose + Material 3 |
| Arquitectura | MVVM (ViewModel + StateFlow), repositorios por entidad |
| Navegación | Navigation Compose |
| Backend | Firebase Authentication + Cloud Firestore |
| Tests | JUnit |
| Control de versiones | Git y GitHub (una rama por funcionalidad, integradas en `develop` y `main`) |

## 🗂️ Estructura del proyecto

```
app/src/main/java/org/insbaixcamp/listadelacompra/
├── data/
│   ├── model/        User, ShoppingList, Category, Product
│   └── repository/   AuthRepository, ListRepository, CategoryRepository,
│                     ProductRepository, ShareRepository
├── navigation/       Routes y NavGraph
├── ui/
│   ├── auth/         Login y registro
│   ├── lists/        Mis listas, tarjetas y diálogos
│   ├── detail/       Detalle de lista: categorías y productos
│   ├── share/        Diálogo de compartir y miembros
│   ├── components/   Componentes reutilizables
│   └── theme/        Colores, formas y tipografía
└── util/             AutoCategorizer (extra)
```

### Modelo de datos en Firestore

```
users/{uid}               nombre y correo
lists/{listId}            name, icon, code, ownerId, members[], createdAt
  ├── categories/{id}     name, icon
  └── products/{id}       name, categoryId, completed, quantity, createdAt
codes/{codigo}            listId, ownerId   (para unirse con el código)
```

## 🛠️ Ejecutar el proyecto desde el código

1. Clona el repositorio y ábrelo con **Android Studio**.
2. Comprueba que `app/google-services.json` existe (conecta la app con el proyecto de Firebase).
3. Haz *Sync Project with Gradle Files* y pulsa **Run** en un emulador o móvil.
4. Tests: `./gradlew testDebugUnitTest`.

## 📚 Documentación

- [`PLAN_LISTA_COMPRA.md`](PLAN_LISTA_COMPRA.md): plan del proyecto por fases y decisiones tomadas.
- [`docs/DISENO.md`](docs/DISENO.md): diseño de pantallas, navegación y estilo visual.

## 👤 Autor

Joel López — 2.º DAM, Institut Baix Camp.
