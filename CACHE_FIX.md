# ✅ SOLUCIÓN: Cache de Productos No Encontrados

## 🔴 Problema Original

```
2025-12-07T21:22:19.032Z ERROR - Error calling external API for product 1000: WebClientRequestException
```

**Se repetía constantemente** porque `Mono.empty()` **NO se cachea** con Spring @Cacheable + Caffeine.

---

## 🎯 Solución Implementada: Wrapper con Optional

### Cambio Clave: `Mono<ProductApiDto>` → `Mono<Optional<ProductApiDto>>`

```java
// ❌ ANTES: Mono.empty() NO se cachea
@Cacheable(value = PRODUCTS_CACHE, key = "#productId")
public Mono<ProductApiDto> getProductById(String productId) {
    return webClient.get()
        .bodyToMono(ProductApiDto.class)
        .onErrorResume(e -> Mono.empty());  // ← NO SE CACHEA
}

// ✅ AHORA: Optional.empty() SÍ se cachea
@Cacheable(value = PRODUCTS_CACHE, key = "#productId")
public Mono<Optional<ProductApiDto>> getProductById(String productId) {
    return webClient.get()
        .bodyToMono(ProductApiDto.class)
        .map(Optional::of)                           // Producto encontrado
        .onErrorResume(e -> Mono.just(Optional.empty()));  // ✅ SE CACHEA
}
```

---

## 📊 Funcionamiento

### Primera petición a producto 1000 (no existe):
```
1. HTTP call → timeout/404
2. onErrorResume → Mono.just(Optional.empty())
3. Spring Cache cachea: "1000" → Optional.empty()  ✅
4. Log: "Error calling external API for product 1000"
```

### Segunda petición a producto 1000:
```
1. Cache HIT → Optional.empty() (instantáneo)
2. NO hay HTTP call  ✅
3. NO hay log de error  ✅
4. Retorna Mono.empty() al servicio
```

---

## 🔧 Componentes Modificados

### 1. ProductApiClient.java
```java
// Retorna Mono<Optional<ProductApiDto>>
public Mono<Optional<ProductApiDto>> getProductById(String productId) {
    return webClient.get()
        .bodyToMono(ProductApiDto.class)
        .map(Optional::of)  // Wrap en Optional
        .onErrorResume(WebClientResponseException.NotFound.class, e -> 
            Mono.just(Optional.empty()))
        .onErrorResume(TimeoutException.class, e -> 
            Mono.just(Optional.empty()))
        .onErrorResume(e -> 
            Mono.just(Optional.empty()))  // Cualquier error
        .defaultIfEmpty(Optional.empty());
}
```

### 2. LoadProductAdapter.java
```java
// Unwrap Optional<ProductApiDto> → Mono<Product>
public Mono<Product> loadProduct(ProductId productId) {
    return productApiClient.getProductById(productId.value())
        .flatMap(optional -> optional
            .map(dto -> Mono.just(mapper.toDomain(dto)))
            .orElse(Mono.empty()));
}
```

---

## ✅ Resultado

### Comportamiento con Cache:

| Escenario | 1ra petición | 2da petición | Logs repetidos |
|-----------|-------------|-------------|----------------|
| Producto existe | HTTP call + cache | Cache HIT | ❌ No |
| Producto no existe (404) | HTTP 404 + cache | Cache HIT | ❌ No |
| Producto timeout | HTTP timeout + cache | Cache HIT | ❌ No |
| Producto error | HTTP error + cache | Cache HIT | ❌ No |

### Performance:

- **Sin cache de errores**: 1000 peticiones a producto inexistente = 1000 HTTP calls (2s cada uno) = 2000s
- **Con cache de errores**: 1000 peticiones a producto inexistente = 1 HTTP call + 999 cache hits = 2s total

**Mejora: 99.9% reducción de llamadas HTTP para productos no encontrados** 🚀

---

## 🎓 Por Qué Funciona

**Spring @Cacheable con Caffeine:**
- ✅ Puede cachear: `Optional.empty()`, `List.of()`, objetos POJO
- ❌ No puede cachear bien: `Mono.empty()`, `null`, valores reactivos vacíos

**Solución:**
- Wrapeamos el resultado en `Optional` (que es serializable y cacheable)
- Spring cachea el `Mono<Optional>` completo
- Caffeine guarda `Optional.empty()` como valor válido
- Al recuperar del cache, unwrapeamos con `flatMap`

---

## 📝 Configuración Final

```yaml
# application.yml
external-apis:
  product-service:
    timeout: 2000  # 2 segundos
```

```java
// CacheConfig.java
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("products", "similarIds");
    cacheManager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(5, TimeUnit.MINUTES)  // TTL: 5 min
        .recordStats());
    return cacheManager;
}
```

**Productos no encontrados se cachean durante 5 minutos, evitando llamadas HTTP repetidas.**

---

✅ **PROBLEMA RESUELTO: Los logs de error ya NO se repiten constantemente.**

