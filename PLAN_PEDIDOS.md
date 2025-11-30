# Plan de extensión a App de Pedidos

Este documento describe cómo extender el proyecto actual del parcial para convertirlo en una **app de pedidos** con:
- Un único tipo de usuario (ya existente).
- Gestión de **productos** (catálogo).
- Gestión de **órdenes/pedidos**.
- **Carrito de compras**.
- Imágenes del menú en **Firebase Storage**.
- Notificaciones push con **Firebase Cloud Messaging (FCM)**.

## 1. Estado actual (resumen corto)

- Autenticación con email/contraseña y Google (`LoginActivity`, `UserRepository`).
- Usuario en Room (`User`, `UserDAO`, `AppDatabase`) con sync opcional a Firestore (`FirebaseManager`).
- `HomeActivity` muestra info del usuario, estado de sincronización, navegación a Perfil y Mapa.
- `PreferenceManager` maneja sesión, recordar usuario y email actual.
- Arquitectura "offline‑first" para usuario: Room como origen local, Firestore como remoto, campos `pendingSync` y `lastUpdated`.

## 2. Nuevas funcionalidades a integrar

1. **Modelo de datos**
   - Nuevas entidades Room:
     - `Product` (producto del menú).
     - `Order` (pedido del usuario).
     - `OrderItem` (ítems dentro de un pedido).
     - Opcional `CartItem` (carrito por usuario).
   - Extender `AppDatabase` para incluir estas entidades y sus DAOs.

2. **Sincronización remota (Firestore)**
   - Crear/expandir managers Firebase:
     - `ProductFirebaseManager` para catálogo de productos.
     - `OrderFirebaseManager` para creación y actualización de órdenes.
   - Colecciones sugeridas en Firestore:
     - `products`: catálogo leído por todos.
     - `orders`: pedidos de usuarios (`orders/{orderId}` con campo `userEmail`).

3. **Carrito de compras**
   - Carrito local por usuario (usando Room o estructura en memoria + persistencia ligera).
   - Operaciones: agregar producto, modificar cantidad, eliminar ítems, vaciar carrito.
   - Conversión del carrito actual a `Order` al confirmar pedido.

4. **Interfaz de usuario para pedidos**
   - Rediseñar/extender `HomeActivity` o crear `ProductListActivity`:
     - Listado de productos (RecyclerView) con imagen, nombre, precio, descripción.
     - Acceso al carrito (icono en toolbar o botón flotante).
   - Nuevas pantallas:
     - `CartActivity` para revisar y confirmar pedido.
     - `OrderHistoryActivity` para ver pedidos anteriores y su estado.
     - Opcional `OrderDetailActivity` para ver detalle de un pedido.

5. **Imágenes en Firebase Storage**
   - Bucket de Storage en el mismo proyecto Firebase.
   - Cada `Product` tendrá un campo `imagePath` o `imageUrl` almacenado en Firestore.
   - En la app:
     - Usar Firebase Storage para obtener URL descargable.
     - Usar una librería de imágenes (ej. Glide) para mostrar las imágenes en el catálogo.

6. **Notificaciones push con FCM**
   - Integrar FCM en el proyecto:
     - Servicio `FirebaseMessagingService` para recibir notificaciones.
     - Manejo de token FCM por usuario (guardar en el documento `users/{email}` en Firestore).
   - Notificaciones de pedido:
     - Al crear pedido: "Pedido recibido".
     - Al cambiar estado: "En preparación", "En camino", "Entregado", etc.
   - Para el parcial:
     - Se pueden simular cambios de estado y notificaciones desde la consola de Firebase o desde una pantalla interna de "admin".

7. **Manejo offline para productos y órdenes**
   - Productos:
     - Descarga inicial y cacheo en Room (`ProductDAO`) para lectura offline.
   - Órdenes:
     - Crear pedido local con `pendingSync = true`.
     - Intentar enviar a Firestore; si tiene éxito, actualizar estado local y `pendingSync`.
     - Reintentos similares a `syncPendingUsers` (nuevo método `syncPendingOrders`).

## 3. Diseño por capas

### 3.1 Capa de datos local (Room)

**Entidades sugeridas**

- `Product`:
  - `id` (PK String o autogenerado).
  - `name`, `description`, `price`.
  - `imagePath` (ruta en Storage o URL pública).
  - `isAvailable` (boolean).
  - `lastUpdated` (long).

- `Order`:
  - `id` (PK).
  - `userEmail` (String).
  - `status` (String: `PENDING`, `CONFIRMED`, `PREPARING`, `DELIVERING`, `DELIVERED`, `CANCELLED`).
  - `total` (double).
  - `createdAt`, `updatedAt` (long).
  - `pendingSync` (boolean).

- `OrderItem`:
  - `id` (PK).
  - `orderId` (FK a `Order`).
  - `productId` (FK a `Product`).
  - `quantity` (int).
  - `unitPrice`, `subtotal` (double).

- `CartItem` (opcional):
  - `id` (PK).
  - `userEmail` (String) para separar carritos.
  - `productId` (FK a `Product`).
  - `quantity` (int).
  - `createdAt` (long).

**DAOs**

- `ProductDAO`:
  - `getAllProducts()`.
  - `insertProducts(List<Product>)`.
  - `getProductById(...)`.
  - `clearAndInsertAll(...)` para sincronización completa.

- `OrderDAO`:
  - `insertOrderWithItems(Order, List<OrderItem>)` (transaccional).
  - `getOrdersByUser(email)`.
  - `getOrderWithItems(orderId)`.
  - `getPendingSyncOrders()`.
  - `updateOrderSyncStatus(...)`.

- `CartDAO` (si se usa `CartItem`):
  - `getCartItemsByUser(email)`.
  - `insertOrUpdate(CartItem)`.
  - `deleteItem(...)`.
  - `clearCart(email)`.

**AppDatabase**

- Incluir las nuevas entidades en `@Database(entities = { ... })`.
- Exponer nuevos DAOs.
- Subir versión de DB (1 → 2) y, si es aceptable para el proyecto, usar `fallbackToDestructiveMigration()` para simplificar.

### 3.2 Capa remota (Firestore + Storage)

**Firestore**

- Colección `products`:
  - Doc `productId` con: `name`, `description`, `price`, `imagePath`, `isAvailable`, `lastUpdated`.

- Colección `orders`:
  - Doc `orderId` con: `userEmail`, `status`, `total`, `createdAt`, `updatedAt`.
  - Subcolección `items` con docs de `OrderItem` (`productId`, `quantity`, `unitPrice`, `subtotal`).

**Managers**

- `ProductFirebaseManager`:
  - `fetchAllProducts(Callback<List<Product>>)`.

- `OrderFirebaseManager`:
  - `createOrder(Order, List<OrderItem>, Callback<Order>)`.
  - `updateOrderStatus(orderId, newStatus, Callback<Void>)`.
  - `fetchOrdersByUser(email, Callback<List<Order>>)`.

**Storage**

- Carpeta `products/` en Firebase Storage con archivos `productId.jpg/png`.
- Guardar `imagePath` en Firestore.
- En la app:
  - Helper para traducir `imagePath` a `StorageReference` y luego a URL.
  - Glide para mostrar la imagen en la lista.

### 3.3 Repositories

- `ProductRepository`:
  - Combina `ProductDAO` + `ProductFirebaseManager`.
  - `getProducts(Callback<List<Product>>)` que primero devuelve datos locales y luego intenta refrescar desde remoto.

- `OrderRepository`:
  - Crea pedidos en Room con `pendingSync = true`.
  - Intenta sincronizar pedido a Firestore.
  - `placeOrder(userEmail, List<CartItem>, Callback<Order>)`.
  - `getUserOrders(userEmail, Callback<List<Order>>)`.
  - `syncPendingOrders(Callback<Void>)`.

- `CartRepository` (opcional):
  - Usa `CartDAO` y `PreferenceManager` para conocer `current_user_email`.

### 3.4 Capa de presentación (UI)

- **Home / Catálogo**:
  - Opción 1: convertir `HomeActivity` en catálogo de productos.
  - Opción 2: crear `ProductListActivity` y navegar desde `HomeActivity`.

- **Catálogo de productos**:
  - Layout `activity_product_list.xml` con RecyclerView.
  - `ProductAdapter` con nombre, precio, imagen y botón "Agregar" al carrito.

- **Carrito** (`CartActivity`):
  - Muestra lista de `CartItem` para el usuario actual.
  - Permite modificar cantidades, eliminar ítems, ver total.
  - Botón "Realizar pedido" que llama a `OrderRepository.placeOrder`.

- **Historial de pedidos** (`OrderHistoryActivity`) y **detalle** (`OrderDetailActivity` opcional).

### 3.5 FCM (Notificaciones)

- Añadir dependencia de Firebase Messaging en `build.gradle` del módulo app.
- Crear servicio `AppFirebaseMessagingService` que extienda `FirebaseMessagingService`:
  - `onNewToken(token)`: guardar token en `users/{email}` en Firestore.
  - `onMessageReceived(...)`: mostrar notificación y abrir `OrderDetailActivity`.
- Registrar el servicio en `AndroidManifest.xml`.

## 4. Checklist de implementación y pruebas

### Fase 1: Modelo local y repositorios básicos

- [ ] Definir entidades `Product`, `Order`, `OrderItem` (y opcional `CartItem`).
- [ ] Crear `ProductDAO`, `OrderDAO` y `CartDAO`.
- [ ] Actualizar `AppDatabase` (nuevas entidades y DAOs, versión 2).
- [ ] Compilar el proyecto y asegurar que Room genera correctamente el código.

### Fase 2: UI de catálogo y carrito (local)

- [ ] Diseñar layout para catálogo (`activity_product_list.xml` o modificar `activity_home.xml`).
- [ ] Implementar `ProductAdapter` y RecyclerView de productos.
- [ ] Crear `CartActivity` con layout para lista de ítems y total.
- [ ] Conectar UI con DAOs/Repositories locales (mock de productos si aún no hay Firestore).
- [ ] Probar agregar productos al carrito, cambiar cantidades y ver el total.

### Fase 3: Integración con Firestore y Storage

- [ ] Crear `ProductFirebaseManager` y lógica para obtener lista de productos desde Firestore.
- [ ] Crear `OrderFirebaseManager` para crear pedidos y guardar ítems en subcolección.
- [ ] Integrar `ProductRepository` y `OrderRepository` con los managers Firebase.
- [ ] Configurar Firebase Storage y subir imágenes de productos.
- [ ] Integrar Glide (u otra librería) y mostrar imágenes de Storage en el catálogo.
- [ ] Probar flujo completo: leer catálogo desde Firestore, crear pedidos y verlos reflejados en Firestore.

### Fase 4: Integración de FCM

- [ ] Añadir dependencia de Firebase Messaging y sincronizar gradle.
- [ ] Implementar `AppFirebaseMessagingService` con `onNewToken` y `onMessageReceived`.
- [ ] Guardar el token FCM del usuario en Firestore.
- [ ] Configurar una notificación de prueba desde la consola de Firebase y verificar que llega al dispositivo.
- [ ] Hacer que una notificación de pedido abra la pantalla de detalle/historial.

### Fase 5: Pulido, validaciones y pruebas finales

- [ ] Reutilizar `ValidationUtils` en formularios nuevos si se agregan (dirección, notas, etc.).
- [ ] Ajustar `HomeActivity` para que la navegación hacia catálogo, carrito e historial sea clara.
- [ ] Probar escenario offline: crear pedido sin conexión y luego sincronizarlo cuando vuelve la red.
- [ ] Verificar que las imágenes se cachean correctamente y que la app no falla sin red.
- [ ] Actualizar el `README` del branch describiendo nuevas entidades, flujos de pedido, uso de Storage y FCM.
