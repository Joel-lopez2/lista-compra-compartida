# Diseño de la aplicación — Lista de la compra compartida

Estilo: **colorido y divertido** (rediseño de la rama `feature/diseno`), con Material 3.
Paleta coral + turquesa + amarillo, tema claro y oscuro según el móvil, y cada lista con su propio color y emoji.

## 1. Mapa de navegación

```
Login <-> Registro
   |
   v
Mis listas ----> Detalle de lista ----> Diálogo "Compartir / Miembros"
   |                   |
   |                   +--> Diálogo "Nueva / editar categoría"
   |                   +--> Diálogo "Nuevo / editar producto"
   +--> Diálogo "Nueva / renombrar lista"
```

Rutas (Navigation Compose): `login`, `register`, `lists`, `detail/{listId}`.
Si hay sesión iniciada la app arranca en `lists`; si no, en `login`.

## 2. Pantallas

### 2.1 Login

```
+------------------------------+
|                              |
|          🛒                  |
|    Lista de la compra        |
|                              |
|  [ Correo electrónico      ] |
|  [ Contraseña           👁 ] |
|                              |
|  [      Iniciar sesión     ] |
|                              |
|  ¿No tienes cuenta? Regístrate|
+------------------------------+
```
- Campos obligatorios; correo con formato válido.
- Errores bajo el campo (ej. "Correo o contraseña incorrectos").
- Indicador de progreso mientras se comprueba.

### 2.2 Registro

```
+------------------------------+
|  <  Crear cuenta             |
|                              |
|  [ Nombre                  ] |
|  [ Correo electrónico      ] |
|  [ Contraseña (mín. 6)  👁 ] |
|  [ Repetir contraseña   👁 ] |
|                              |
|  [      Registrarme        ] |
+------------------------------+
```
- Contraseña de 6 caracteres o más y coincidencia entre las dos.
- Al registrarse se crea el documento `users/{uid}`.

### 2.3 Mis listas

```
+------------------------------+
|  Mis listas            ⎋ [salir]
|                              |
|  MIS LISTAS                  |
|  +------------------------+  |
|  | Compra semanal      ⋮ |  |
|  | 12 productos           |  |
|  +------------------------+  |
|  | Fiesta cumpleaños   ⋮ |  |
|  +------------------------+  |
|                              |
|  COMPARTIDAS CONMIGO         |
|  +------------------------+  |
|  | Casa de la playa    👥 |  |
|  | de ana@correo.com      |  |
|  +------------------------+  |
|                          (+) |
+------------------------------+
```
- Dos secciones: propias y compartidas. Las compartidas llevan el icono 👥 y el propietario.
- Menú ⋮ en las propias: Renombrar, Compartir, Eliminar (con confirmación).
- En las compartidas: Salir de la lista.
- Botón flotante (+) para crear una lista (nombre obligatorio).
- Estado vacío: "Aún no tienes listas. Pulsa + para crear la primera".

### 2.4 Detalle de lista

```
+------------------------------+
|  <  Compra semanal   👥  ⋮   |
|                              |
|  🥛 LÁCTEOS                  |
|   ( ) Leche            2 L   |
|   ( ) Yogur                  |
|                              |
|  🍎 FRUTAS Y VERDURAS        |
|   ( ) Manzanas         1 kg  |
|                              |
|  SIN CATEGORÍA               |
|   ( ) Pilas AA               |
|                              |
|  COMPLETADOS (2)          v  |
|   (x) ~~Pan~~                |
|   (x) ~~Huevos~~             |
|                              |
|  [ Añadir producto...    ] + |
+------------------------------+
```
- Productos agrupados por categoría, con la categoría en mayúsculas pequeñas.
- Pulsar el círculo marca/desmarca como completado (tachado y gris; pasa a la sección
  "Completados", que se puede plegar). Nunca se elimina al completarlo.
- Pulsar el producto abre el diálogo de edición; deslizar o menú para eliminar.
- Barra inferior para añadir rápido: se escribe el nombre y se confirma.
  (Más adelante: la categoría se propondrá sola con el extra de autoclasificación.)
- Menú ⋮: Nueva categoría, Gestionar categorías, Compartir.

### 2.5 Diálogos

**Producto:** nombre (obligatorio), cantidad (opcional), categoría (desplegable, incluye "Sin categoría").
**Categoría:** nombre (obligatorio), icono/emoji (opcional).
**Compartir:** campo de correo + botón "Compartir"; debajo, lista de miembros con botón de quitar
(solo el propietario). Mensajes: "Ese usuario no está registrado", "Ya tiene acceso".
**Eliminar:** confirmación con el nombre del elemento. Al borrar una categoría, sus productos pasan a "Sin categoría".

## 3. Estilo visual

### Paleta

| Rol | Claro | Oscuro |
|---|---|---|
| Primario (coral) | `#FF6B57` | tono coral claro |
| Secundario (turquesa) | `#14B8A6` | turquesa claro |
| Terciario (amarillo) | `#FFC233` | amarillo suave |
| Fondo | crema `#FFF8F1` | marrón muy oscuro |

Los valores exactos están en `ui/theme/Color.kt`. Se mantiene desactivado el color dinámico de Android 12+
para que la app tenga siempre la misma identidad.

### Color y emoji por lista
`ui/theme/ListColors.kt` elige uno de 8 estilos (color + emoji: 🛒🥐🧀🥑🥛🍇🍓🥦) a partir del hash del id de la lista.
Así todos los miembros ven la lista igual sin guardar nada extra en Firestore.

### Tipografía y formas
- Títulos en negrita (`Type.kt`); categorías como "pastillas" teñidas con el color de la lista.
- Esquinas: 24 dp en tarjetas, 16 dp en campos y filas de producto, pastilla completa en botones y barra de añadir.
- Cabecera con degradado coral → naranja y emoji en Login/Registro (`AuthHeader`).
- Barra de progreso "N de M comprados" en cada tarjeta de lista y en el detalle.
- Barra superior del detalle con el color de la lista; los productos marcados tiñen su fondo con animación.

### Componentes Compose previstos
`Scaffold` + `TopAppBar`, `LazyColumn`, `Card`, `OutlinedTextField`, `Checkbox`/`IconToggleButton`,
`ExtendedFloatingActionButton`, `LinearProgressIndicator`, `AlertDialog`, `DropdownMenu`, `Snackbar` para errores y confirmaciones.

## 4. Textos principales (español)

Todos los textos irán en `res/values/strings.xml` (nada escrito a mano en el código).
Ejemplos: `login_title`, `email_label`, `password_label`, `lists_empty`, `new_list`,
`share_list`, `user_not_found`, `completed_section`, `uncategorized`.
