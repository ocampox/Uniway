#!/usr/bin/env python3
"""
=============================================================================
ADVANCED SECURITY TESTING SUITE - UniWay Foro Estudiantil
=============================================================================
Script avanzado de pruebas de seguridad usando Python para tests más complejos
Incluye pruebas de JWT, timing attacks, y análisis de vulnerabilidades OWASP
=============================================================================
"""

import requests
import json
import time
import hashlib
import base64
import jwt
from datetime import datetime, timedelta
import threading
import sys
import os

# Configuración
BASE_URL = "http://192.168.1.4:8080"
TEST_EMAIL = "security.test@pascualbravo.edu.co"
TEST_PASSWORD = "TestPassword123!"
RESULTS_FILE = "advanced_security_results.txt"

# Colores para terminal
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    PURPLE = '\033[0;35m'
    CYAN = '\033[0;36m'
    NC = '\033[0m'  # No Color

class SecurityTester:
    def __init__(self):
        self.session = requests.Session()
        self.results = []
        self.jwt_token = None
        
    def log(self, message, level="INFO"):
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        color = {
            "INFO": Colors.BLUE,
            "SUCCESS": Colors.GREEN,
            "WARNING": Colors.YELLOW,
            "ERROR": Colors.RED,
            "CRITICAL": Colors.PURPLE
        }.get(level, Colors.NC)
        
        formatted_msg = f"{color}[{timestamp}] [{level}]{Colors.NC} {message}"
        print(formatted_msg)
        
        # Guardar en archivo sin colores
        clean_msg = f"[{timestamp}] [{level}] {message}"
        self.results.append(clean_msg)
        
    def save_results(self):
        with open(RESULTS_FILE, 'w', encoding='utf-8') as f:
            f.write("UNIWAY ADVANCED SECURITY TEST RESULTS\n")
            f.write("=" * 50 + "\n")
            f.write(f"Test Date: {datetime.now()}\n")
            f.write(f"Target: {BASE_URL}\n")
            f.write("=" * 50 + "\n\n")
            
            for result in self.results:
                f.write(result + "\n")
                
    def test_server_availability(self):
        """Test 1: Verificar disponibilidad del servidor"""
        self.log("🚀 Iniciando pruebas avanzadas de seguridad...")
        self.log("📡 1. VERIFICANDO DISPONIBILIDAD DEL SERVIDOR")
        
        try:
            response = self.session.get(f"{BASE_URL}/", timeout=10)
            if response.status_code in [200, 404]:
                self.log("Servidor disponible y respondiendo", "SUCCESS")
                return True
            else:
                self.log(f"Servidor responde con código inesperado: {response.status_code}", "WARNING")
                return True
        except requests.exceptions.RequestException as e:
            self.log(f"Error de conectividad: {e}", "ERROR")
            return False
            
    def test_jwt_vulnerabilities(self):
        """Test 2: Vulnerabilidades en JWT"""
        self.log("🔐 2. PRUEBAS DE VULNERABILIDADES JWT")
        
        # Test 2.1: JWT None Algorithm Attack
        self.log("2.1 Probando ataque 'None Algorithm' en JWT...")
        
        # Crear un JWT malicioso con algoritmo 'none'
        header = {"alg": "none", "typ": "JWT"}
        payload = {
            "sub": "admin@pascualbravo.edu.co",
            "role": "ADMINISTRATION",
            "exp": int(time.time()) + 3600
        }
        
        # Codificar sin firma (algoritmo none)
        header_b64 = base64.urlsafe_b64encode(json.dumps(header).encode()).decode().rstrip('=')
        payload_b64 = base64.urlsafe_b64encode(json.dumps(payload).encode()).decode().rstrip('=')
        malicious_jwt = f"{header_b64}.{payload_b64}."
        
        # Probar con JWT malicioso
        headers = {"Authorization": f"Bearer {malicious_jwt}"}
        try:
            response = self.session.get(f"{BASE_URL}/users/profile", headers=headers)
            if response.status_code == 200:
                self.log("VULNERABILIDAD CRÍTICA: JWT acepta algoritmo 'none'", "CRITICAL")
            else:
                self.log("JWT correctamente rechaza algoritmo 'none'", "SUCCESS")
        except Exception as e:
            self.log(f"Error en test JWT: {e}", "WARNING")
            
        # Test 2.2: JWT Secret Brute Force (simulado)
        self.log("2.2 Simulando ataque de fuerza bruta a secreto JWT...")
        weak_secrets = ["secret", "123456", "password", "uniway", "admin"]
        
        for secret in weak_secrets:
            try:
                # Intentar decodificar un JWT hipotético con secretos débiles
                test_payload = {"test": "data"}
                test_token = jwt.encode(test_payload, secret, algorithm="HS256")
                decoded = jwt.decode(test_token, secret, algorithms=["HS256"])
                self.log(f"Secreto débil detectado en simulación: {secret}", "WARNING")
            except:
                continue
                
        self.log("Simulación de fuerza bruta JWT completada", "INFO")
        
    def test_timing_attacks(self):
        """Test 3: Ataques de timing"""
        self.log("⏱️ 3. PRUEBAS DE TIMING ATTACKS")
        
        # Test 3.1: Timing attack en login
        self.log("3.1 Analizando tiempos de respuesta en login...")
        
        # Usuario existente vs no existente
        existing_user = {"email": "admin@pascualbravo.edu.co", "password": "wrongpassword"}
        nonexistent_user = {"email": "nonexistent@test.com", "password": "wrongpassword"}
        
        times_existing = []
        times_nonexistent = []
        
        for i in range(5):
            # Test con usuario existente
            start_time = time.time()
            self.session.post(f"{BASE_URL}/auth/login", json=existing_user)
            end_time = time.time()
            times_existing.append(end_time - start_time)
            
            time.sleep(0.1)
            
            # Test con usuario no existente
            start_time = time.time()
            self.session.post(f"{BASE_URL}/auth/login", json=nonexistent_user)
            end_time = time.time()
            times_nonexistent.append(end_time - start_time)
            
            time.sleep(0.1)
            
        avg_existing = sum(times_existing) / len(times_existing)
        avg_nonexistent = sum(times_nonexistent) / len(times_nonexistent)
        
        time_diff = abs(avg_existing - avg_nonexistent)
        
        if time_diff > 0.1:  # Diferencia significativa
            self.log(f"Posible timing attack: diferencia de {time_diff:.3f}s", "WARNING")
        else:
            self.log("Tiempos de respuesta consistentes - protegido contra timing attacks", "SUCCESS")
            
    def test_sql_injection_advanced(self):
        """Test 4: Inyección SQL avanzada"""
        self.log("💉 4. PRUEBAS AVANZADAS DE INYECCIÓN SQL")
        
        # Payloads de inyección SQL más sofisticados
        sql_payloads = [
            "' OR '1'='1' --",
            "' UNION SELECT NULL,NULL,NULL --",
            "'; DROP TABLE users; --",
            "' OR 1=1 LIMIT 1 OFFSET 0 --",
            "' AND (SELECT COUNT(*) FROM users) > 0 --",
            "' OR SLEEP(5) --",
            "' OR pg_sleep(5) --"
        ]
        
        for payload in sql_payloads:
            self.log(f"4.1 Probando payload: {payload[:20]}...")
            
            # Test en login
            malicious_login = {
                "email": f"test{payload}",
                "password": "password"
            }
            
            start_time = time.time()
            try:
                response = self.session.post(f"{BASE_URL}/auth/login", json=malicious_login, timeout=10)
                end_time = time.time()
                
                # Verificar si hay indicios de inyección SQL
                if end_time - start_time > 5:  # Respuesta muy lenta (posible SLEEP)
                    self.log("VULNERABILIDAD: Posible inyección SQL con SLEEP", "CRITICAL")
                elif response.status_code == 500:
                    self.log("Posible error de base de datos - revisar logs", "WARNING")
                elif "syntax error" in response.text.lower():
                    self.log("VULNERABILIDAD: Error de sintaxis SQL expuesto", "CRITICAL")
                else:
                    self.log("Payload rechazado correctamente", "SUCCESS")
                    
            except requests.exceptions.Timeout:
                self.log("VULNERABILIDAD: Timeout - posible inyección SQL con SLEEP", "CRITICAL")
            except Exception as e:
                self.log(f"Error en test SQL: {e}", "WARNING")
                
    def test_xss_advanced(self):
        """Test 5: XSS avanzado"""
        self.log("🕷️ 5. PRUEBAS AVANZADAS DE XSS")
        
        xss_payloads = [
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "javascript:alert('XSS')",
            "<svg onload=alert('XSS')>",
            "';alert('XSS');//",
            "<iframe src=javascript:alert('XSS')></iframe>",
            "<body onload=alert('XSS')>",
            "<<SCRIPT>alert('XSS')//<</SCRIPT>"
        ]
        
        for payload in xss_payloads:
            self.log(f"5.1 Probando XSS payload: {payload[:30]}...")
            
            # Test en registro de usuario
            xss_register = {
                "email": "xsstest@test.com",
                "password": "Test123!",
                "fullName": payload,
                "role": "STUDENT",
                "studentId": "XSS123",
                "program": "Test Program"
            }
            
            try:
                response = self.session.post(f"{BASE_URL}/auth/register", json=xss_register)
                
                if payload in response.text:
                    self.log("VULNERABILIDAD: XSS no filtrado", "CRITICAL")
                else:
                    self.log("XSS correctamente filtrado", "SUCCESS")
                    
            except Exception as e:
                self.log(f"Error en test XSS: {e}", "WARNING")
                
    def test_csrf_protection(self):
        """Test 6: Protección CSRF"""
        self.log("🛡️ 6. PRUEBAS DE PROTECCIÓN CSRF")
        
        # Simular ataque CSRF
        self.log("6.1 Verificando protección contra CSRF...")
        
        # Intentar crear un post sin token CSRF
        csrf_post = {
            "content": "Post creado via CSRF",
            "postType": "GENERAL",
            "userEmail": "test@test.com"
        }
        
        try:
            response = self.session.post(f"{BASE_URL}/posts/dev", json=csrf_post)
            
            if response.status_code == 200:
                self.log("VULNERABILIDAD: Endpoint vulnerable a CSRF", "WARNING")
            elif response.status_code == 403:
                self.log("Protección CSRF activa", "SUCCESS")
            else:
                self.log(f"Respuesta inesperada a CSRF: {response.status_code}", "INFO")
                
        except Exception as e:
            self.log(f"Error en test CSRF: {e}", "WARNING")
            
    def test_rate_limiting_advanced(self):
        """Test 7: Rate limiting avanzado"""
        self.log("⚡ 7. PRUEBAS AVANZADAS DE RATE LIMITING")
        
        # Test 7.1: Rate limiting con múltiples hilos
        self.log("7.1 Probando rate limiting con múltiples hilos...")
        
        def make_login_request():
            login_data = {"email": "test@test.com", "password": "wrongpassword"}
            return self.session.post(f"{BASE_URL}/auth/login", json=login_data)
            
        # Crear múltiples hilos para simular ataque DDoS
        threads = []
        responses = []
        
        def thread_worker():
            response = make_login_request()
            responses.append(response.status_code)
            
        for i in range(20):
            thread = threading.Thread(target=thread_worker)
            threads.append(thread)
            thread.start()
            
        # Esperar a que terminen todos los hilos
        for thread in threads:
            thread.join()
            
        # Analizar respuestas
        rate_limited = sum(1 for code in responses if code == 429)
        
        if rate_limited > 0:
            self.log(f"Rate limiting detectado: {rate_limited}/20 requests bloqueados", "SUCCESS")
        else:
            self.log("No se detectó rate limiting - considerar implementar", "WARNING")
            
    def test_information_disclosure(self):
        """Test 8: Divulgación de información"""
        self.log("📄 8. PRUEBAS DE DIVULGACIÓN DE INFORMACIÓN")
        
        # Test 8.1: Endpoints de información sensible
        sensitive_endpoints = [
            "/actuator/health",
            "/actuator/info",
            "/actuator/env",
            "/swagger-ui.html",
            "/api-docs",
            "/h2-console",
            "/.env",
            "/config",
            "/admin"
        ]
        
        for endpoint in sensitive_endpoints:
            self.log(f"8.1 Verificando endpoint sensible: {endpoint}")
            
            try:
                response = self.session.get(f"{BASE_URL}{endpoint}")
                
                if response.status_code == 200:
                    # Verificar si contiene información sensible
                    sensitive_keywords = ["password", "secret", "key", "token", "database"]
                    
                    for keyword in sensitive_keywords:
                        if keyword.lower() in response.text.lower():
                            self.log(f"VULNERABILIDAD: Información sensible en {endpoint}", "CRITICAL")
                            break
                    else:
                        self.log(f"Endpoint {endpoint} accesible pero sin información crítica", "WARNING")
                else:
                    self.log(f"Endpoint {endpoint} correctamente protegido", "SUCCESS")
                    
            except Exception as e:
                self.log(f"Error verificando {endpoint}: {e}", "WARNING")
                
    def run_all_tests(self):
        """Ejecutar todas las pruebas de seguridad"""
        print("=" * 70)
        print("🔒 UNIWAY ADVANCED SECURITY TESTING SUITE")
        print("=" * 70)
        print(f"Fecha: {datetime.now()}")
        print(f"Target: {BASE_URL}")
        print("=" * 70)
        
        if not self.test_server_availability():
            self.log("Servidor no disponible - abortando pruebas", "ERROR")
            return
            
        # Ejecutar todas las pruebas
        self.test_jwt_vulnerabilities()
        self.test_timing_attacks()
        self.test_sql_injection_advanced()
        self.test_xss_advanced()
        self.test_csrf_protection()
        self.test_rate_limiting_advanced()
        self.test_information_disclosure()
        
        # Guardar resultados
        self.save_results()
        
        # Mostrar resumen
        self.show_summary()
        
    def show_summary(self):
        """Mostrar resumen de resultados"""
        print("\n" + "=" * 70)
        print("📊 RESUMEN DE PRUEBAS AVANZADAS")
        print("=" * 70)
        
        total_tests = len(self.results)
        critical_issues = len([r for r in self.results if "CRITICAL" in r])
        warnings = len([r for r in self.results if "WARNING" in r])
        successes = len([r for r in self.results if "SUCCESS" in r])
        
        print(f"Total de pruebas: {total_tests}")
        print(f"{Colors.RED}Vulnerabilidades críticas: {critical_issues}{Colors.NC}")
        print(f"{Colors.YELLOW}Advertencias: {warnings}{Colors.NC}")
        print(f"{Colors.GREEN}Pruebas exitosas: {successes}{Colors.NC}")
        
        print(f"\n📄 Resultados detallados guardados en: {RESULTS_FILE}")
        
        # Recomendaciones
        print(f"\n{Colors.CYAN}🛡️ RECOMENDACIONES CRÍTICAS:{Colors.NC}")
        print("-" * 50)
        print("1. Implementar validación estricta de JWT")
        print("2. Usar secretos JWT fuertes y rotativos")
        print("3. Implementar rate limiting agresivo")
        print("4. Sanitizar todas las entradas de usuario")
        print("5. Implementar protección CSRF")
        print("6. Ocultar endpoints de información sensible")
        print("7. Usar HTTPS en producción")
        print("8. Implementar logging de seguridad")
        
        if critical_issues > 0:
            print(f"\n{Colors.RED}⚠️ ATENCIÓN: Se encontraron {critical_issues} vulnerabilidades críticas{Colors.NC}")
        else:
            print(f"\n{Colors.GREEN}✅ No se encontraron vulnerabilidades críticas{Colors.NC}")

if __name__ == "__main__":
    tester = SecurityTester()
    tester.run_all_tests()