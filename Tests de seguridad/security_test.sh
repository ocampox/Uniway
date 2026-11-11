#!/bin/bash

# =============================================================================
# SCRIPT DE PRUEBAS DE SEGURIDAD - UniWay Foro Estudiantil
# =============================================================================
# Este script realiza pruebas de seguridad automatizadas en la aplicación
# UniWay para identificar vulnerabilidades comunes en aplicaciones web/móviles
# =============================================================================

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
BASE_URL="http://192.168.1.4:8080"
TEST_EMAIL="security.test@pascualbravo.edu.co"
TEST_PASSWORD="TestPassword123!"
RESULTS_FILE="security_test_results.txt"

# Función para logging
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$RESULTS_FILE"
}

log_success() {
    echo -e "${GREEN}[✓ PASS]${NC} $1" | tee -a "$RESULTS_FILE"
}

log_warning() {
    echo -e "${YELLOW}[⚠ WARNING]${NC} $1" | tee -a "$RESULTS_FILE"
}

log_error() {
    echo -e "${RED}[✗ FAIL]${NC} $1" | tee -a "$RESULTS_FILE"
}

# Función para hacer requests HTTP
make_request() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local headers="$4"
    
    if [ -n "$data" ]; then
        curl -s -X "$method" "$BASE_URL$endpoint" \
             -H "Content-Type: application/json" \
             ${headers:+-H "$headers"} \
             -d "$data" \
             -w "\nHTTP_CODE:%{http_code}\nRESPONSE_TIME:%{time_total}\n"
    else
        curl -s -X "$method" "$BASE_URL$endpoint" \
             ${headers:+-H "$headers"} \
             -w "\nHTTP_CODE:%{http_code}\nRESPONSE_TIME:%{time_total}\n"
    fi
}

# =============================================================================
# INICIO DE PRUEBAS DE SEGURIDAD
# =============================================================================

echo "================================================================="
echo "🔒 UNIWAY SECURITY TESTING SUITE"
echo "================================================================="
echo "Fecha: $(date)"
echo "URL Base: $BASE_URL"
echo "================================================================="

# Limpiar archivo de resultados
> "$RESULTS_FILE"

log "🚀 Iniciando pruebas de seguridad para UniWay..."

# =============================================================================
# 1. PRUEBAS DE CONECTIVIDAD Y DISPONIBILIDAD
# =============================================================================

log "📡 1. PRUEBAS DE CONECTIVIDAD"
echo "-----------------------------------------------------------------"

# Test 1.1: Verificar que el servidor esté corriendo
log "1.1 Verificando disponibilidad del servidor..."
response=$(make_request "GET" "/" "" "")
http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)

if [ "$http_code" = "404" ] || [ "$http_code" = "200" ]; then
    log_success "Servidor está corriendo (HTTP $http_code)"
else
    log_error "Servidor no disponible (HTTP $http_code)"
    exit 1
fi

# Test 1.2: Verificar endpoints principales
endpoints=("/posts" "/auth/login" "/teacher-recommendations" "/users/profile")
for endpoint in "${endpoints[@]}"; do
    log "1.2 Probando endpoint: $endpoint"
    response=$(make_request "GET" "$endpoint" "" "")
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
        log_success "Endpoint $endpoint responde correctamente (HTTP $http_code)"
    else
        log_warning "Endpoint $endpoint respuesta inesperada (HTTP $http_code)"
    fi
done

# =============================================================================
# 2. PRUEBAS DE AUTENTICACIÓN Y AUTORIZACIÓN
# =============================================================================

log "🔐 2. PRUEBAS DE AUTENTICACIÓN"
echo "-----------------------------------------------------------------"

# Test 2.1: Intento de acceso sin autenticación
log "2.1 Probando acceso sin autenticación a endpoints protegidos..."
protected_endpoints=("/users/profile")
for endpoint in "${protected_endpoints[@]}"; do
    response=$(make_request "GET" "$endpoint" "" "")
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    
    if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
        log_success "Endpoint $endpoint correctamente protegido (HTTP $http_code)"
    else
        log_error "VULNERABILIDAD: Endpoint $endpoint accesible sin autenticación (HTTP $http_code)"
    fi
done

# Test 2.2: Prueba de login con credenciales inválidas
log "2.2 Probando login con credenciales inválidas..."
invalid_login='{"email":"invalid@test.com","password":"wrongpassword"}'
response=$(make_request "POST" "/auth/login" "$invalid_login" "")
http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)

if [ "$http_code" = "401" ] || [ "$http_code" = "400" ]; then
    log_success "Login rechaza credenciales inválidas correctamente (HTTP $http_code)"
else
    log_error "VULNERABILIDAD: Login acepta credenciales inválidas (HTTP $http_code)"
fi

# Test 2.3: Prueba de inyección SQL en login
log "2.3 Probando inyección SQL en login..."
sql_injection='{"email":"admin@test.com'\'' OR 1=1 --","password":"anything"}'
response=$(make_request "POST" "/auth/login" "$sql_injection" "")
http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)

if [ "$http_code" = "401" ] || [ "$http_code" = "400" ]; then
    log_success "Sistema protegido contra inyección SQL básica (HTTP $http_code)"
else
    log_error "VULNERABILIDAD CRÍTICA: Posible inyección SQL (HTTP $http_code)"
fi

# =============================================================================
# 3. PRUEBAS DE VALIDACIÓN DE ENTRADA
# =============================================================================

log "📝 3. PRUEBAS DE VALIDACIÓN DE ENTRADA"
echo "-----------------------------------------------------------------"

# Test 3.1: XSS en registro de usuario
log "3.1 Probando XSS en registro de usuario..."
xss_payload='{"email":"test@test.com","password":"Test123!","fullName":"<script>alert('\''XSS'\'')</script>","role":"STUDENT","studentId":"XSS123","program":"Test Program"}'
response=$(make_request "POST" "/auth/register" "$xss_payload" "")
http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)

if echo "$response" | grep -q "<script>"; then
    log_error "VULNERABILIDAD: XSS no filtrado en registro"
else
    log_success "Sistema filtra correctamente scripts XSS"
fi

# Test 3.2: Campos excesivamente largos
log "3.2 Probando campos excesivamente largos..."
long_string=$(python3 -c "print('A' * 10000)")
long_payload="{\"email\":\"test@test.com\",\"password\":\"Test123!\",\"fullName\":\"$long_string\",\"role\":\"STUDENT\"}"
response=$(make_request "POST" "/auth/register" "$long_payload" "")
http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)

if [ "$http_code" = "400" ] || [ "$http_code" = "413" ]; then
    log_success "Sistema rechaza campos excesivamente largos (HTTP $http_code)"
else
    log_warning "Sistema acepta campos muy largos - revisar límites (HTTP $http_code)"
fi

# Test 3.3: Caracteres especiales maliciosos
log "3.3 Probando caracteres especiales maliciosos..."
special_chars='{"email":"test@test.com","password":"Test123!","fullName":"../../../etc/passwd","role":"STUDENT"}'
response=$(make_request "POST" "/auth/register" "$special_chars" "")
http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)

if echo "$response" | grep -q "passwd"; then
    log_error "VULNERABILIDAD: Posible path traversal"
else
    log_success "Sistema maneja correctamente caracteres especiales"
fi

# =============================================================================
# 4. PRUEBAS DE RATE LIMITING Y DOS
# =============================================================================

log "⚡ 4. PRUEBAS DE RATE LIMITING"
echo "-----------------------------------------------------------------"

# Test 4.1: Múltiples intentos de login
log "4.1 Probando múltiples intentos de login (DoS básico)..."
for i in {1..10}; do
    response=$(make_request "POST" "/auth/login" "$invalid_login" "")
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    
    if [ "$i" -gt 5 ] && [ "$http_code" = "429" ]; then
        log_success "Rate limiting activado después de múltiples intentos"
        break
    elif [ "$i" = "10" ]; then
        log_warning "No se detectó rate limiting - considerar implementar"
    fi
    
    sleep 0.1
done

# =============================================================================
# 5. PRUEBAS DE CONFIGURACIÓN DE SEGURIDAD
# =============================================================================

log "⚙️ 5. PRUEBAS DE CONFIGURACIÓN DE SEGURIDAD"
echo "-----------------------------------------------------------------"

# Test 5.1: Headers de seguridad
log "5.1 Verificando headers de seguridad..."
headers_response=$(curl -s -I "$BASE_URL/")

security_headers=("X-Frame-Options" "X-Content-Type-Options" "X-XSS-Protection" "Strict-Transport-Security")
for header in "${security_headers[@]}"; do
    if echo "$headers_response" | grep -qi "$header"; then
        log_success "Header de seguridad presente: $header"
    else
        log_warning "Header de seguridad faltante: $header"
    fi
done

# Test 5.2: Información sensible en respuestas
log "5.2 Verificando exposición de información sensible..."
response=$(make_request "GET" "/actuator/health" "" "")
if echo "$response" | grep -qi "password\|secret\|key"; then
    log_error "VULNERABILIDAD: Información sensible expuesta en /actuator/health"
else
    log_success "No se detectó información sensible en endpoints de salud"
fi

# Test 5.3: Métodos HTTP no permitidos
log "5.3 Probando métodos HTTP no estándar..."
methods=("TRACE" "OPTIONS" "PUT" "DELETE")
for method in "${methods[@]}"; do
    response=$(make_request "$method" "/" "" "")
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    
    if [ "$http_code" = "405" ] || [ "$http_code" = "404" ]; then
        log_success "Método $method correctamente restringido (HTTP $http_code)"
    else
        log_warning "Método $method permitido - revisar configuración (HTTP $http_code)"
    fi
done

# =============================================================================
# 6. PRUEBAS ESPECÍFICAS DE LA APLICACIÓN
# =============================================================================

log "📱 6. PRUEBAS ESPECÍFICAS DE UNIWAY"
echo "-----------------------------------------------------------------"

# Test 6.1: Acceso a recomendaciones sin autenticación
log "6.1 Verificando acceso a recomendaciones de profesores..."
response=$(make_request "GET" "/teacher-recommendations" "" "")
http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)

if [ "$http_code" = "200" ]; then
    log_success "Recomendaciones accesibles públicamente (diseño intencional)"
elif [ "$http_code" = "401" ]; then
    log_success "Recomendaciones protegidas por autenticación"
else
    log_warning "Respuesta inesperada en recomendaciones (HTTP $http_code)"
fi

# Test 6.2: Creación de posts maliciosos
log "6.2 Probando creación de posts con contenido malicioso..."
malicious_post='{"content":"<script>alert('\''XSS in post'\'')</script>","postType":"GENERAL","priority":"NORMAL","userEmail":"test@test.com"}'
response=$(make_request "POST" "/posts/dev" "$malicious_post" "")
http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)

if echo "$response" | grep -q "<script>"; then
    log_error "VULNERABILIDAD: XSS en contenido de posts"
else
    log_success "Contenido de posts correctamente filtrado"
fi

# =============================================================================
# 7. PRUEBAS DE PRIVACIDAD Y DATOS SENSIBLES
# =============================================================================

log "🔒 7. PRUEBAS DE PRIVACIDAD"
echo "-----------------------------------------------------------------"

# Test 7.1: Enumeración de usuarios
log "7.1 Probando enumeración de usuarios..."
for id in {1..5}; do
    response=$(make_request "GET" "/users/$id" "" "")
    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    
    if [ "$http_code" = "200" ]; then
        log_warning "Posible enumeración de usuarios - endpoint /users/$id accesible"
        break
    elif [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
        log_success "Usuarios protegidos contra enumeración"
        break
    fi
done

# Test 7.2: Acceso a datos de otros usuarios
log "7.2 Verificando aislamiento de datos de usuarios..."
# Este test requeriría tokens válidos para ser más efectivo
log_warning "Test de aislamiento de datos requiere tokens válidos - implementar en pruebas manuales"

# =============================================================================
# RESUMEN DE RESULTADOS
# =============================================================================

echo ""
echo "================================================================="
echo "📊 RESUMEN DE PRUEBAS DE SEGURIDAD"
echo "================================================================="

# Contar resultados
total_tests=$(grep -c "\[.*\]" "$RESULTS_FILE")
passed_tests=$(grep -c "✓ PASS" "$RESULTS_FILE")
warnings=$(grep -c "⚠ WARNING" "$RESULTS_FILE")
failed_tests=$(grep -c "✗ FAIL" "$RESULTS_FILE")

echo "Total de pruebas ejecutadas: $total_tests"
echo -e "${GREEN}Pruebas exitosas: $passed_tests${NC}"
echo -e "${YELLOW}Advertencias: $warnings${NC}"
echo -e "${RED}Pruebas fallidas: $failed_tests${NC}"

echo ""
echo "📄 Resultados detallados guardados en: $RESULTS_FILE"

# Recomendaciones de seguridad
echo ""
echo "🛡️ RECOMENDACIONES DE SEGURIDAD:"
echo "-----------------------------------------------------------------"
echo "1. Implementar rate limiting para prevenir ataques de fuerza bruta"
echo "2. Agregar headers de seguridad (X-Frame-Options, CSP, etc.)"
echo "3. Validar y sanitizar todas las entradas de usuario"
echo "4. Implementar logging de seguridad para auditorías"
echo "5. Usar HTTPS en producción"
echo "6. Implementar validación de tokens JWT más estricta"
echo "7. Considerar implementar CAPTCHA para formularios críticos"
echo "8. Revisar permisos de endpoints públicos vs protegidos"

echo ""
echo "✅ Pruebas de seguridad completadas."
echo "================================================================="