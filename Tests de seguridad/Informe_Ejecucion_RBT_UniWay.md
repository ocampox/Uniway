# Informe de ejecución RBT - UniWay

**Plan de referencia:** TP-UNIWAY-001  
**Fecha del análisis:** 16 de noviembre de 2026  
**Alcance:** API REST Spring Boot, integración esperada con Android y MySQL, y riesgos P1/P2 del plan de pruebas.

## 1. Resumen ejecutivo

El estado actual del código **no es aprobable para una entrega que requiera autenticación y autorización**. La revisión estática confirma que Spring Security permite todas las rutas y que varios controladores toman la identidad desde `userId`, `studentId` o `authorId` enviados por el cliente, en vez de derivarla de un JWT validado. Esto materializa los riesgos **R-01, R-02 y R-03** como incidencias críticas.

Se ejecutó la suite `Tests de seguridad/test_riesgos_principales.py` contra `http://localhost:8080` después de iniciar el backend con una base MySQL aislada en Docker. Las 11 pruebas se ejecutaron: **5 aprobaron y 6 fallaron**. La prueba de rating fuera de rango aprobó; las verificaciones de rutas, perfiles, listado de usuarios y endpoint de desarrollo fallaron. Los recursos usados para los casos de autorización pertenecieron únicamente a la base sintética de pruebas.

| Resultado | Cantidad | Interpretación |
|---|---:|---|
| Fallos confirmados por revisión estática | 12 | Veredictos de caso que evidencian 10 defectos de seguridad o lógica agrupados. |
| Pass por revisión estática | 7 | Existe un control en código o base de datos; requiere retest con backend activo. |
| No ejecutados / bloqueados | 3 | Casos sin comprobación dinámica por falta de entorno disponible o datos de prueba. |
| Tests ejecutados contra API | 11 | Resultado dinámico: 5 aprobados, 6 fallidos, 0 omitidos. |

## 2. Alcance, método y evidencia

### Método aplicado

1. Se revisó el plan TP-UNIWAY-001 y se priorizaron R-01, R-02, R-03 y R-08.
2. Se revisó la suite de riesgos principales y se ejecutó de modo no destructivo contra `localhost:8080`.
3. Se comprobó la disponibilidad local: el puerto 8080 no estaba atendiendo.
4. Se analizaron los filtros de seguridad, controladores, servicios y restricciones SQL relacionados con autenticación, identidad, permisos, reacciones y calificación de docentes.
5. Se contrastó la evidencia histórica en `security_test_results.txt` y `advanced_security_results.txt`. Es evidencia del 4 de noviembre de 2025 contra otro entorno; no acredita el comportamiento del build actual.

### Convenciones de resultado

- **PASS - estático:** el control está presente en el código o en la base de datos; falta confirmación de ejecución.
- **FAIL - estático:** el código permite o implementa el comportamiento inseguro descrito.
- **NO EJECUTADO:** no fue posible verificar el escenario de forma dinámica. No debe convertirse en PASS.

## 3. Interpretación de la suite automática

La suite `test_riesgos_principales.py` está bien orientada a R-01, R-02, R-03, R-05, R-06, R-08 y R-09. Comprueba rutas sin token, JWT malformados, exposición de usuarios, endpoint de hash, dominio institucional, mensajes de login y recuperación. Las pruebas de edición ajena y rating fuera de rango se mantienen desactivadas por defecto para no alterar datos reales.

**Ejecución inicial observada (antes de iniciar el entorno):**

```text
Ran 11 tests
OK (skipped=11)
```

- Los ocho controles no destructivos se omitieron porque no fue posible conectar a `localhost:8080`.
- Las tres pruebas con modificación se omitieron porque `UNIWAY_ENABLE_MUTATION_TESTS` no estaba habilitada ni existían tokens y recursos de prueba configurados.
- No se ejecutaron los scripts históricos porque apuntan a una dirección LAN distinta y algunos envían cargas o realizan ráfagas de solicitudes. Deben usarse solo contra un entorno de pruebas autorizado y aislado.

**Ejecución dinámica completa posterior:**

```text
Ran 11 tests
FAILED (failures=6)
```

| Prueba | Resultado dinámico | Interpretación |
|---|---|---|
| Smoke del backend | PASS (200) | Backend y base aislada disponibles. |
| Perfil sin token | FAIL (400) | No devuelve 401/403; el encabezado requerido por el controlador reemplaza una política de seguridad real. |
| Perfil con JWT malformado | FAIL (200) | El token alterado no se valida y se devuelve un perfil. |
| Listado de usuarios público | FAIL (200) | Se expone el listado sin autenticar. |
| Generador de hash público | FAIL (200) | Endpoint de desarrollo expuesto. |
| Correo no institucional | PASS (400) | El registro rechaza el dominio externo. |
| Login inválido sin secretos | PASS (400) | Rechazo observado sin las cadenas sensibles revisadas por la suite. |
| Recuperación con correo externo | PASS (400) | El flujo rechaza el dominio externo. |
| Edición de post ajeno con ID manipulado | FAIL (400) | No devuelve 401/403. En este caso el ID manipulado no existe, por lo que evidencia manejo HTTP incorrecto, no una modificación exitosa. |
| Edición de comentario ajeno con ID manipulado | FAIL (500) | Error interno ante la solicitud; debe corregirse para responder de forma controlada y verificar JWT. |
| Rating 6 | PASS (400) | Se rechazó el valor fuera del rango 1..5. |

## 4. Hoja de Ejecución de Casos de Prueba

| ID del Caso | Módulo | Escenario | Prioridad | Resultado esperado | Estado | Observaciones |
|---|---|---|---|---|---|---|
| CP-AUT-01 | Registro | Registro con correo institucional válido | P1 | Enviar código y no activar hasta confirmación | NO EJECUTADO | Backend no disponible. El flujo debe retestearse; el registro inicia código. |
| CP-AUT-02 | Registro | Correo no institucional | P1 | Rechazo descriptivo | PASS - estático | `AuthController` compara el sufijo institucional y retorna 400. |
| CP-AUT-03 | Registro | Código inválido o vencido | P1 | Rechazo y cuenta inactiva | FAIL - estático | `complete-registration` no invoca validación del código antes de crear usuario. |
| CP-AUT-04 | Login | Credenciales válidas | P1 | JWT válido y acceso a inicio | NO EJECUTADO | La generación de token existe, pero no hubo backend ni datos de prueba. |
| CP-AUT-05 | Login | Contraseña incorrecta | P1 | Denegar sin revelar datos sensibles | PASS - estático | El servicio de autenticación usa verificación de contraseña; requiere retest de respuesta y mensajes. |
| CP-AUT-06 | Sesión | Token omitido, vencido o alterado | P1 | HTTP 401/403 e impedir operación | FAIL - estático | El filtro de seguridad usa `anyRequest().permitAll()` y los controladores no validan el JWT. |
| CP-AUT-07 | Recuperación | Código/token de recuperación inválido o vencido | P1 | Denegar cambio de clave | PASS - estático | `reset-password` llama a `emailService.verifyCode`; requiere prueba dinámica de expiración. |
| CP-SEC-01 | Autorización | `userId` distinto del sujeto del JWT | P1 | Identidad derivada del JWT y discrepancia rechazada | FAIL - estático | No existe filtro JWT activo; endpoints usan IDs suministrados por el cliente. |
| CP-POS-01 | Foro | Crear publicación autenticada | P1 | Persistir con autor autenticado | FAIL - estático | `POST /posts` selecciona un usuario por defecto de desarrollo, sin identidad autenticada. |
| CP-POS-02 | Foro | Editar publicación propia | P1 | Actualizar contenido propio | FAIL - estático | La autorización se calcula con `userId` del cuerpo, no con una identidad autenticada. |
| CP-POS-03 | Foro | Editar publicación ajena | P1 | HTTP 403 | FAIL - estático | Un atacante que conozca el ID del autor puede enviarlo como propio; la ruta está abierta. |
| CP-POS-04 | Foro | Administración elimina publicación ajena | P1 | Permitido solo a Administración y auditado | FAIL - estático | La lógica de rol existe, pero la identidad puede ser suplantada y no hay evidencia de auditoría. |
| CP-REA-01 | Reacciones | Like sin reacción previa | P1 | Crear una reacción e incrementar contador | PASS - estático | Servicio implementa toggle y SQL mantiene unicidad usuario-post; falta retest transaccional. |
| CP-REA-02 | Reacciones | Repetir Like | P1 | Eliminar reacción y decrementar contador | PASS - estático | La rama de toggle elimina el LIKE existente; requiere ejecución con datos aislados. |
| CP-REA-03 | Reacciones | Cambiar Dislike a Like | P1 | Un único registro y contadores consistentes | PASS - estático | El servicio cambia el tipo y la base define unicidad usuario-post. |
| CP-COM-01 | Comentarios | Crear comentario válido | P1 | Persistir y asociar al usuario correcto | FAIL - estático | El autor se recibe en `authorId` del cuerpo sin autenticación. |
| CP-COM-02 | Comentarios | Editar/eliminar comentario ajeno | P1 | Denegar al no autor | FAIL - estático | El servicio evalúa permisos con el `userId` proporcionado por la solicitud. |
| CP-REC-01 | Recomendaciones | Rating válido entre 1 y 5 | P1 | Persistir recomendación y rating válido | FAIL - estático | El rango se valida, pero `studentId` procede del cliente sin autenticación. |
| CP-REC-02 | Recomendaciones | Rating 0 o 6 | P2 | Rechazo sin persistencia | PASS - estático | Servicio valida 1..5 y MySQL dispone de `CHECK` de rango. |
| CP-REC-03 | Recomendaciones | Eliminar propia, ajena o como Administración | P1 | Solo autor o Administración pueden eliminar | FAIL - estático | Se comprueba rol/autoría sobre el `userId` recibido, no sobre JWT validado. |
| CP-INT-01 | Integración | Backend indisponible desde Android | P2 | Mensaje amigable sin cierre inesperado | NO EJECUTADO | Requiere emulador/dispositivo y backend controlado. |
| CP-SEC-02 | Seguridad | Revisar respuestas, logs y configuración | P1 | No exponer secretos o credenciales | FAIL - estático | Hay valores sensibles predeterminados en configuración y endpoints de desarrollo expuestos. |

## 5. Registro de Defectos

| ID de Incidencia | Componente | Severidad | Descripción del Problema | Pasos para Reproducir | Comportamiento Real vs Esperado |
|---|---|---|---|---|---|
| INC-001 | `SecurityConfig` | Crítica | La cadena de seguridad permite todas las solicitudes. | Iniciar API; solicitar una ruta sensible sin token o con token alterado. | **Real:** no hay regla global que exija autenticación. **Esperado:** rutas privadas deben responder 401/403. |
| INC-002 | `UserController` | Crítica | Perfil lee y actualiza el primer usuario de la lista; no valida el token recibido. | Llamar `GET` o `PUT /users/profile` con cualquier valor en `Authorization`. | **Real:** se opera sobre el primer usuario disponible. **Esperado:** usar exclusivamente el usuario identificado por JWT válido. |
| INC-003 | Posts, comentarios y recomendaciones | Crítica | Suplantación de identidad: acciones sensibles aceptan `userId`, `authorId` o `studentId` desde la solicitud. | Enviar un ID de otra persona a `PUT /posts/{id}`, `PUT /comments/{id}`, `POST /comments` o recomendaciones. | **Real:** permisos se evalúan contra ID controlado por cliente. **Esperado:** ignorar esos IDs para autorización y derivar sujeto/rol desde JWT. |
| INC-004 | `AuthController` | Crítica | El alta final no verifica el código de registro. | Enviar `POST /auth/complete-registration` con un código inválido o vencido. | **Real:** el código no se comprueba antes de crear cuenta. **Esperado:** rechazar y no persistir cuenta hasta verificar código vigente. |
| INC-005 | `PostController` y rutas de desarrollo | Alta | Endpoints de desarrollo permiten crear posts con un correo o usuario por defecto; el generador de hash está expuesto. | Llamar `POST /posts`, `POST /posts/dev` o `GET /posts/hash/{valor}` sin autenticación. | **Real:** rutas disponibles en la misma superficie de la API. **Esperado:** eliminar, deshabilitar por perfil o restringir estrictamente a desarrollo local. |
| INC-006 | Configuración backend | Alta | Existen valores sensibles predeterminados para servicios y JWT, además de logging de depuración. | Revisar el archivo de configuración y arrancar sin sobrescribir variables. | **Real:** un despliegue puede usar valores por defecto inseguros. **Esperado:** secretos fuera del repositorio, obligatorios por entorno y sin logging sensible. |
| INC-007 | Validación de contenido | Alta | Evidencia histórica reporta XSS al crear posts; el flujo actual no muestra saneamiento en el controlador o servicio. | En entorno aislado, crear/editar post o comentario con payload XSS inofensivo y verificar renderizado. | **Real histórico:** se reportó contenido XSS en posts. **Esperado:** almacenar/mostrar texto de forma segura, sin ejecutar marcado activo. |
| INC-008 | Recuperación y registro | Media | El modo de desarrollo expone códigos de verificación en la respuesta. | Ejecutar registro o recuperación en el perfil actual. | **Real:** el método de modo desarrollo retorna `devCode`. **Esperado:** nunca exponer códigos en ambientes no locales; controlar mediante perfiles. |
| INC-009 | Disponibilidad / autenticación | Media | No hay evidencia de rate limiting; los resultados históricos no detectaron respuesta 429. | En entorno aislado, enviar una ráfaga limitada de logins inválidos. | **Real histórico:** no se detectó limitación. **Esperado:** limitar intentos y responder 429 o aplicar bloqueo progresivo. |
| INC-010 | Moderación de comentarios | Alta | Aprobar comentarios no exige rol administrativo ni autenticación. | Llamar `POST /comments/{id}/approve` sin token. | **Real:** la ruta invoca aprobación directamente. **Esperado:** solo Administración autenticada puede aprobar. |

## 6. Riesgos, conclusiones y decisión RBT

| Riesgo | Estado actual | Decisión |
|---|---|---|
| R-01 - JWT y rutas protegidas | Abierto, crítico | Bloquear aprobación hasta implementar autenticación JWT real, filtros y reglas de autorización. |
| R-02 - Suplantación de identidad | Abierto, crítico | Eliminar confianza en IDs de cliente para identidad y permisos. |
| R-03 - Autoría y roles | Abierto, crítico | Aplicar control en backend basado en identidad autenticada; después ejecutar matriz Autor/No autor/Administración. |
| R-04 - Reacciones y contadores | Control parcial estático | Ejecutar transiciones concurrentes y validar contadores en MySQL tras corregir R-01/R-02. |
| R-05/R-09 - Registro y recuperación | Control parcial; falla crítica en alta final | Restaurar verificación de código en registro y retestear expiración. |
| R-06 - Exposición de datos | Abierto, alto | Retirar endpoints de desarrollo, secretos predeterminados, códigos de desarrollo y exposición de documentación en ambientes no locales. |
| R-08 - Rating | Control de límite presente | Mantener validación doble servicio/base y ejecutar prueba de 0, 1, 5 y 6 con identidad autenticada. |

**Decisión de calidad:** **NO APROBADO** para cierre de pruebas P1. Los defectos INC-001 a INC-004 deben corregirse y verificarse con pruebas dinámicas antes de aceptar los riesgos residuales.

## 7. Próxima ejecución recomendada

1. Crear una base MySQL exclusiva de pruebas y dos usuarios sintéticos: autor y no autor, además de uno con rol Administración.
2. Iniciar backend con secretos inyectados por variables de entorno y sin perfiles/endpoints de desarrollo públicos.
3. Configurar `UNIWAY_BASE_URL`, iniciar el backend y ejecutar `python "Tests de seguridad/test_riesgos_principales.py"`.
4. Habilitar solo en la base aislada `UNIWAY_ENABLE_MUTATION_TESTS=1`, tokens sintéticos y los IDs de post, comentario y recomendación desechables.
5. Repetir CP-AUT-06, CP-SEC-01, CP-POS-03, CP-COM-02 y CP-REC-03; deben retornar 401/403 para el no autor y no cambiar los datos.
6. Adjuntar salida de consola, versión del build, fecha, ejecutor, consultas MySQL de verificación y capturas Android a la hoja de ejecución.

## 8. Fuentes revisadas

- Plan oficial: `C:\Users\ALEJANDRO\Documents\UNIVERSIDAD\7.SEPTIMO SEMESTRE\PRUEBAS DE SOFTWARE\Plan de pruebas Uniway.pdf`.
- Suite actual: `Tests de seguridad/test_riesgos_principales.py`.
- Evidencia histórica: `Tests de seguridad/security_test_results.txt` y `Tests de seguridad/advanced_security_results.txt`.
- Implementación revisada: configuración de seguridad, controladores/servicios de usuarios, autenticación, posts, comentarios y recomendaciones; esquema `database/uniway_db.sql`.
