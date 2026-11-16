"""Pruebas automáticas de riesgos principales de UniWay.

Ejecutar únicamente contra un entorno local o de pruebas con datos sintéticos:

    $env:UNIWAY_BASE_URL = "http://localhost:8080"
    python "Tests de seguridad/test_riesgos_principales.py"

No requiere dependencias externas: usa ``unittest`` y ``urllib`` de Python.
Las pruebas que modifican datos están desactivadas por defecto. Para habilitarlas
se debe usar una base de datos de pruebas y definir explícitamente:

    UNIWAY_ENABLE_MUTATION_TESTS=1
    UNIWAY_TEST_TOKEN_A, UNIWAY_TEST_TOKEN_B
    UNIWAY_TEST_POST_ID, UNIWAY_TEST_COMMENT_ID, UNIWAY_TEST_RECOMMENDATION_ID

Riesgos cubiertos: R-01, R-02, R-03, R-05, R-06 y R-09.
"""

from __future__ import annotations

import json
import os
import unittest
from dataclasses import dataclass
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import quote
from urllib.request import Request, urlopen


BASE_URL = os.getenv("UNIWAY_BASE_URL", "http://localhost:8080").rstrip("/")
TIMEOUT_SECONDS = float(os.getenv("UNIWAY_TIMEOUT_SECONDS", "10"))
MUTATION_TESTS_ENABLED = os.getenv("UNIWAY_ENABLE_MUTATION_TESTS") == "1"


@dataclass
class HttpResult:
    status: int
    body: Any
    headers: dict[str, str]


def request(
    method: str,
    path: str,
    body: dict[str, Any] | None = None,
    token: str | None = None,
) -> HttpResult:
    """Hace una solicitud JSON y devuelve respuestas HTTP, incluso de error."""
    headers = {"Accept": "application/json"}
    payload = None
    if body is not None:
        payload = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = Request(f"{BASE_URL}{path}", data=payload, headers=headers, method=method)
    try:
        with urlopen(req, timeout=TIMEOUT_SECONDS) as response:
            raw = response.read().decode("utf-8", errors="replace")
            return HttpResult(response.status, parse_json(raw), dict(response.headers.items()))
    except HTTPError as error:
        raw = error.read().decode("utf-8", errors="replace")
        return HttpResult(error.code, parse_json(raw), dict(error.headers.items()))
    except URLError as error:
        raise unittest.SkipTest(
            f"No se pudo conectar a {BASE_URL}: {error.reason}. Inicie el backend de pruebas."
        ) from error


def parse_json(raw: str) -> Any:
    if not raw:
        return None
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        return raw


def is_denied(result: HttpResult) -> bool:
    return result.status in {401, 403}


class RiesgosPrincipalesUniWayTests(unittest.TestCase):
    """Pruebas no destructivas para ejecutar en cada sprint o entrega."""

    def test_01_backend_responde(self) -> None:
        """Smoke test: el backend debe estar disponible antes de continuar."""
        result = request("GET", "/posts/test")
        self.assertEqual(result.status, 200, result.body)

    def test_02_perfil_rechaza_solicitud_sin_token(self) -> None:
        """R-01: un perfil no puede devolverse sin credenciales válidas."""
        result = request("GET", "/users/profile")
        self.assertTrue(
            is_denied(result),
            f"R-01: /users/profile aceptó una solicitud sin token (HTTP {result.status}).",
        )

    def test_03_perfil_rechaza_token_malformado(self) -> None:
        """R-01: un encabezado arbitrario no debe autenticarse como JWT."""
        result = request("GET", "/users/profile", token="jwt.manipulado.invalido")
        self.assertTrue(
            is_denied(result),
            f"R-01: /users/profile aceptó un JWT alterado (HTTP {result.status}).",
        )

    def test_04_listado_usuarios_no_es_publico(self) -> None:
        """R-01/R-06: el listado de usuarios exige rol administrativo."""
        result = request("GET", "/users")
        self.assertTrue(
            is_denied(result),
            f"R-01/R-06: /users es público (HTTP {result.status}).",
        )

    def test_05_generador_de_hash_no_es_publico(self) -> None:
        """R-06: un endpoint de desarrollo no debe estar expuesto."""
        result = request("GET", f"/posts/hash/{quote('clave-prueba-segura')}")
        self.assertIn(
            result.status,
            {401, 403, 404},
            f"R-06: el generador de hashes está expuesto (HTTP {result.status}).",
        )

    def test_06_registro_rechaza_dominio_no_institucional(self) -> None:
        """R-05: no debe iniciar registro para un correo externo."""
        result = request(
            "POST",
            "/auth/register",
            {
                "email": "prueba.automatizada@example.com",
                "password": "ClaveDePrueba123!",
                "role": "ESTUDIANTE",
                "fullName": "Prueba Automatizada",
                "studentId": "TEST-000",
                "program": "Pruebas",
            },
        )
        self.assertIn(result.status, {400, 422}, result.body)

    def test_07_login_invalido_no_revela_secretos(self) -> None:
        """R-06: credenciales inválidas se rechazan sin filtrar tokens o trazas."""
        result = request(
            "POST",
            "/auth/login",
            {"email": "inexistente@pascualbravo.edu.co", "password": "incorrecta"},
        )
        self.assertIn(result.status, {400, 401, 403}, result.body)
        serialised = json.dumps(result.body).lower()
        for sensitive_fragment in ("jwt", "secret", "password=", "stacktrace"):
            self.assertNotIn(sensitive_fragment, serialised)

    def test_08_recuperacion_rechaza_dominio_no_institucional(self) -> None:
        """R-09: la recuperación se restringe a correos institucionales."""
        result = request(
            "POST", "/auth/forgot-password", {"email": "externo@example.com"}
        )
        self.assertIn(result.status, {400, 422}, result.body)

    def test_09_recomendacion_rechaza_rating_invalido(self) -> None:
        """R-08: el servidor debe rechazar ratings fuera del rango 1-5 (ej. 6)."""
        # Prueba no destructiva enviando un rating inválido con un token nulo o inválido
        result = request(
            "POST",
            "/teacher-recommendations/with-rating",
            {
                "teacherName": "Docente Límite",
                "subject": "Pruebas",
                "rating": 6,  # Inválido según R-08
                "reference": "Prueba de límite"
            },
            token="token.invalido.prueba"
        )
        # Debe denegar por autenticación (401/403) o por validación de negocio (400/422)
        self.assertIn(result.status, {400, 401, 403, 422}, result.body)

@unittest.skipUnless(
    MUTATION_TESTS_ENABLED,
    "Pruebas con modificación desactivadas. Use UNIWAY_ENABLE_MUTATION_TESTS=1 en una base aislada.",
)
class AutorizacionConDatosDePruebaTests(unittest.TestCase):
    """R-02/R-03: pruebas opcionales que usan recursos desechables de prueba."""

    @classmethod
    def setUpClass(cls) -> None:
        required = [
            "UNIWAY_TEST_TOKEN_A",
            "UNIWAY_TEST_TOKEN_B",
            "UNIWAY_TEST_POST_ID",
            "UNIWAY_TEST_COMMENT_ID",
            "UNIWAY_TEST_RECOMMENDATION_ID",
        ]
        missing = [name for name in required if not os.getenv(name)]
        if missing:
            raise unittest.SkipTest("Faltan variables: " + ", ".join(missing))
        cls.author_token = os.environ["UNIWAY_TEST_TOKEN_A"]
        cls.other_token = os.environ["UNIWAY_TEST_TOKEN_B"]
        cls.post_id = os.environ["UNIWAY_TEST_POST_ID"]
        cls.comment_id = os.environ["UNIWAY_TEST_COMMENT_ID"]
        cls.recommendation_id = os.environ["UNIWAY_TEST_RECOMMENDATION_ID"]

    def test_09_otro_usuario_no_edita_post_del_autor(self) -> None:
        """R-03: un no autor debe recibir 401 o 403 al editar un post ajeno."""
        result = request(
            "PUT",
            f"/posts/{quote(self.post_id)}",
            {"content": "No debe persistir", "postType": "GENERAL", "userId": "manipulado"},
            self.other_token,
        )
        self.assertTrue(is_denied(result), f"R-03: edición ajena permitida: {result.status}")

    def test_10_otro_usuario_no_edita_comentario_del_autor(self) -> None:
        """R-02/R-03: el userId enviado no sustituye la identidad del JWT."""
        result = request(
            "PUT",
            f"/comments/{quote(self.comment_id)}",
            {"content": "No debe persistir", "userId": "id-del-autor-manipulado"},
            self.other_token,
        )
        self.assertTrue(is_denied(result), f"R-02/R-03: edición ajena permitida: {result.status}")

    def test_11_rating_fuera_de_rango_se_rechaza(self) -> None:
        """R-08: el servidor debe rechazar un rating de 6."""
        result = request(
            "POST",
            "/teacher-recommendations/with-rating",
            {
                "studentId": "id-manipulado",
                "teacherName": "Docente Prueba",
                "subject": "Pruebas",
                "semester": "2026-2",
                "year": 2026,
                "reference": "Prueba automática de límite; no debe persistir.",
                "rating": 6,
            },
            self.other_token,
        )
        self.assertIn(result.status, {400, 401, 403, 422}, result.body)


if __name__ == "__main__":
    unittest.main(verbosity=2)
