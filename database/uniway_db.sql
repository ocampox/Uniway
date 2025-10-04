-- UniWay Database Schema
-- Base de datos para el foro estudiantil UniWay
-- 
-- Última actualización: Eliminación del rol SECURITY
-- Solo mantiene roles: STUDENT y ADMINISTRATION
-- 
-- Servidor: 127.0.0.1
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

CREATE DATABASE uniway_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;



SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

USE uniway_db;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `uniway_db`
--

DELIMITER $$
--
-- Procedimientos
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetPostComments` (IN `post_id_param` VARCHAR(36))   BEGIN
    SELECT c.*, u.full_name as author_name, u.role as author_role
    FROM comments c
    JOIN users u ON c.author_id = u.id
    WHERE c.post_id = post_id_param AND c.is_approved = TRUE
    ORDER BY c.created_at ASC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetPostsByType` (IN `post_type_param` VARCHAR(20))   BEGIN
    SELECT * FROM post_with_author 
    WHERE post_type = post_type_param 
    ORDER BY is_pinned DESC, created_at DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GetUserPosts` (IN `user_id_param` VARCHAR(36))   BEGIN
    SELECT * FROM post_with_author 
    WHERE author_id = user_id_param 
    ORDER BY created_at DESC;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `comments`
--

CREATE TABLE `comments` (
  `id` varchar(36) NOT NULL,
  `post_id` varchar(36) NOT NULL,
  `author_id` varchar(36) NOT NULL,
  `content` text NOT NULL,
  `is_approved` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Volcado de datos para la tabla `comments`
--

INSERT INTO `comments` (`id`, `post_id`, `author_id`, `content`, `is_approved`, `created_at`, `updated_at`) VALUES
('5da10c06-ebdf-4ef6-b58f-2a5e7823a7a1', 'a69382b1-812d-41ce-88f9-0d664d32c3e7', 'e0b3ca24-4b5d-4c17-a940-d67843d7be83', 'Interesante modificado 3', 1, '2025-10-03 00:39:13', '2025-10-03 01:38:45');

-- --------------------------------------------------------

-- Los nombres de profesores se almacenan directamente en 'student_teachers'
-- --------------------------------------------------------

--
-- Disparadores `comments`
--
DELIMITER $$
CREATE TRIGGER `update_post_comment_count` AFTER INSERT ON `comments` FOR EACH ROW BEGIN
    UPDATE posts 
    SET updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.post_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `moderation_logs`
--

CREATE TABLE `moderation_logs` (
  `id` varchar(36) NOT NULL,
  `moderator_id` varchar(36) NOT NULL,
  `target_type` enum('POST','COMMENT') NOT NULL,
  `target_id` varchar(36) NOT NULL,
  `action` enum('APPROVE','REJECT','DELETE','PIN','UNPIN') NOT NULL,
  `reason` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `posts`
--

CREATE TABLE `posts` (
  `id` varchar(36) NOT NULL,
  `author_id` varchar(36) NOT NULL,
  `content` text NOT NULL,
  `post_type` enum('GENERAL','NEWS','ALERT','ANNOUNCEMENT') DEFAULT 'GENERAL',
  `priority` enum('LOW','NORMAL','HIGH','URGENT') DEFAULT 'NORMAL',
  `is_pinned` tinyint(1) DEFAULT 0,
  `is_alert` tinyint(1) DEFAULT 0,
  `is_approved` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `comment_count` int(11) DEFAULT 0,
  `dislike_count` int(11) DEFAULT NULL,
  `like_count` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `posts`
--

INSERT INTO `posts` (`id`, `author_id`, `content`, `post_type`, `priority`, `is_pinned`, `is_alert`, `is_approved`, `created_at`, `updated_at`, `comment_count`, `dislike_count`, `like_count`) VALUES
('5304da5c-b416-4ab0-b8a4-f0cacd8f78d2', 'e0b3ca24-4b5d-4c17-a940-d67843d7be83', 'Este es un post nuevo', 'GENERAL', 'NORMAL', 0, 0, 1, '2025-10-03 00:24:05', '2025-10-03 01:08:18', 0, 0, 1),
('a69382b1-812d-41ce-88f9-0d664d32c3e7', '1a29067c-7c87-4872-a628-a13b2b44ae22', 'Este es El primer post de prueba.', 'GENERAL', 'NORMAL', 0, 0, 1, '2025-09-24 05:00:20', '2025-10-03 00:39:13', 1, 0, 1);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `post_with_author`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `post_with_author` (
`id` varchar(36)
,`author_id` varchar(36)
,`content` text
,`post_type` enum('GENERAL','NEWS','ALERT','ANNOUNCEMENT')
,`priority` enum('LOW','NORMAL','HIGH','URGENT')
,`is_pinned` tinyint(1)
,`is_alert` tinyint(1)
,`is_approved` tinyint(1)
,`created_at` timestamp
,`updated_at` timestamp
,`author_name` varchar(255)
,`author_role` enum('STUDENT','ADMINISTRATION')
,`author_image` varchar(500)
,`like_count` bigint(21)
,`dislike_count` bigint(21)
,`comment_count` bigint(21)
);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `reactions`
--

CREATE TABLE `reactions` (
  `id` varchar(36) NOT NULL,
  `post_id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `reaction_type` enum('LIKE','DISLIKE') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `type` enum('LIKE','DISLIKE') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Disparadores `reactions`
--
DELIMITER $$
CREATE TRIGGER `update_post_like_count` AFTER INSERT ON `reactions` FOR EACH ROW BEGIN
    UPDATE posts 
    SET updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.post_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `users`
-- Roles disponibles:
-- - STUDENT: Estudiantes regulares (pueden crear posts, comentar, dar likes)
-- - ADMINISTRATION: Administradores (pueden moderar contenido, editar/eliminar cualquier post)
--

CREATE TABLE `users` (
  `id` varchar(36) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('STUDENT','ADMINISTRATION') NOT NULL COMMENT 'Rol del usuario: STUDENT o ADMINISTRATION',
  `full_name` varchar(255) NOT NULL,
  `student_id` varchar(50) DEFAULT NULL,
  `program` varchar(255) DEFAULT NULL,
  `profile_image_url` varchar(500) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `users`
--

INSERT INTO `users` (`id`, `email`, `password_hash`, `role`, `full_name`, `student_id`, `program`, `profile_image_url`, `phone`, `address`, `is_active`, `created_at`, `updated_at`) VALUES
('1a29067c-7c87-4872-a628-a13b2b44ae22', 'alejandro.ocampo751@pascualbravo.edu.co', '$2a$10$vxFqW3xE6F6BAyyC9Buoau3XEToRGjHoYQU9.z3ErFQhGnCkUZivi', 'ADMINISTRATION', 'Alejandro Ocampo', 'TL 1758687878987', 'Ingeniería de Software', NULL, NULL, NULL, 1, '2025-09-24 04:24:39', '2025-09-24 04:24:39'),
('e0b3ca24-4b5d-4c17-a940-d67843d7be83', 'juan.perez822@pascualbravo.edu.co', '$2a$10$8c6IWbUalYSlbrN/.HX0uuELpHaYi1QQboLxGTCUF0trkNlwFkWV2', 'STUDENT', 'Juan David Perez', 'TL 1759447943959', 'Ingeniería de Software', NULL, NULL, NULL, 1, '2025-10-02 23:32:27', '2025-10-02 23:32:27');

-- --------------------------------------------------------

--
-- Estructura para la vista `post_with_author`
--
DROP TABLE IF EXISTS `post_with_author`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `post_with_author`  AS SELECT `p`.`id` AS `id`, `p`.`author_id` AS `author_id`, `p`.`content` AS `content`, `p`.`post_type` AS `post_type`, `p`.`priority` AS `priority`, `p`.`is_pinned` AS `is_pinned`, `p`.`is_alert` AS `is_alert`, `p`.`is_approved` AS `is_approved`, `p`.`created_at` AS `created_at`, `p`.`updated_at` AS `updated_at`, `u`.`full_name` AS `author_name`, `u`.`role` AS `author_role`, `u`.`profile_image_url` AS `author_image`, count(distinct `r`.`id`) AS `like_count`, count(distinct case when `r`.`reaction_type` = 'DISLIKE' then `r`.`id` end) AS `dislike_count`, count(distinct `c`.`id`) AS `comment_count` FROM (((`posts` `p` join `users` `u` on(`p`.`author_id` = `u`.`id`)) left join `reactions` `r` on(`p`.`id` = `r`.`post_id` and `r`.`reaction_type` = 'LIKE')) left join `comments` `c` on(`p`.`id` = `c`.`post_id` and `c`.`is_approved` = 1)) WHERE `p`.`is_approved` = 1 GROUP BY `p`.`id` ;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `comments`
--
ALTER TABLE `comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_post_id` (`post_id`),
  ADD KEY `idx_author_id` (`author_id`),
  ADD KEY `idx_created_at` (`created_at`);

-- --------------------------------------------------------
-- NOTA: Índices de tablas 'teachers' y 'teacher_reviews' eliminados
-- Sistema simplificado sin estas tablas
-- --------------------------------------------------------

--
-- Indices de la tabla `moderation_logs`
--
ALTER TABLE `moderation_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_moderator_id` (`moderator_id`),
  ADD KEY `idx_target_type_id` (`target_type`,`target_id`),
  ADD KEY `idx_created_at` (`created_at`);



--
-- Indices de la tabla `posts`
--
ALTER TABLE `posts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_author_id` (`author_id`),
  ADD KEY `idx_post_type` (`post_type`),
  ADD KEY `idx_priority` (`priority`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_is_pinned` (`is_pinned`),
  ADD KEY `idx_posts_approved_created` (`is_approved`,`created_at`),
  ADD KEY `idx_posts_pinned_priority` (`is_pinned`,`priority`);

--
-- Indices de la tabla `reactions`
--
ALTER TABLE `reactions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_post_reaction` (`user_id`,`post_id`),
  ADD UNIQUE KEY `UKaeq6cssia730m2nihyav05ui1` (`user_id`,`post_id`),
  ADD KEY `idx_post_id` (`post_id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_reactions_post_type` (`post_id`,`reaction_type`);



--
-- Indices de la tabla `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `student_id` (`student_id`);

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `comments`
--
ALTER TABLE `comments`
  ADD CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

-- --------------------------------------------------------
-- NOTA: Restricciones de tabla 'teacher_reviews' eliminadas
-- Tabla eliminada del sistema simplificado
-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `student_teachers`
-- Sistema simplificado de recomendaciones de profesores con calificación por estrellas
--
CREATE TABLE `student_teachers` (
  `id` varchar(36) NOT NULL,
  `student_id` varchar(36) NOT NULL,
  `teacher_name` varchar(255) NOT NULL COMMENT 'Nombre completo del profesor',
  `subject` varchar(255) NOT NULL,
  `semester` varchar(50) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  `reference` TEXT COMMENT 'Referencia y recomendación del estudiante sobre el profesor',
  `rating` int(11) DEFAULT NULL COMMENT 'Calificación del profesor de 1 a 5 estrellas',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  CONSTRAINT `chk_rating_range` CHECK (`rating` IS NULL OR (`rating` >= 1 AND `rating` <= 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

-- Volcado de datos para la tabla `student_teachers`
--

INSERT INTO `student_teachers` (`id`, `student_id`, `teacher_name`, `subject`, `semester`, `year`, `reference`, `rating`, `is_active`, `created_at`, `updated_at`) VALUES
('155b8724-d247-48fc-ad7a-381f7f7023b5', '1a29067c-7c87-4872-a628-a13b2b44ae22', 'Juan Carlos Perez', 'Matematicas', '2024-2', 2024, 'Lo recomiendo porque es mero Bien.', 5, 1, '2025-10-02 22:44:09', '2025-10-03 00:33:50'),
('1800fe4e-0038-4a89-bf90-eb71be89b29f', '1a29067c-7c87-4872-a628-a13b2b44ae22', 'Maria Carmona', 'Filosofia', '2024-2', 2024, 'La recomiendo porque si.', 4, 1, '2025-10-02 23:29:36', '2025-10-03 00:33:47'),
('d87e232f-76bc-49ec-96eb-c04dfe3ea7fc', 'e0b3ca24-4b5d-4c17-a940-d67843d7be83', 'Felipe Mendez', 'Programacion 3', '2025-2', 2025, 'Lo recomiendo porque ensena Bien.', 5, 1, '2025-10-03 00:22:49', '2025-10-03 01:40:45');



--
-- Estructura de tabla para la tabla `teacher_recommendation_reactions`
-- Sistema de likes/dislikes para recomendaciones de profesores
--
CREATE TABLE `teacher_recommendation_reactions` (
  `id` varchar(36) NOT NULL,
  `recommendation_id` varchar(36) NOT NULL COMMENT 'ID de la recomendación (student_teacher)',
  `user_id` varchar(36) NOT NULL COMMENT 'ID del usuario que reacciona',
  `reaction_type` enum('LIKE','DISLIKE') NOT NULL COMMENT 'Tipo de reacción',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
COMMENT='Reacciones (likes/dislikes) a recomendaciones de profesores';

-- --------------------------------------------------------
-- Estructura de tabla para la tabla `verification_codes`
--

CREATE TABLE `verification_codes` (
  `id` varchar(36) NOT NULL,
  `code` varchar(6) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `is_used` bit(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Vista para recomendaciones con contadores de reacciones y calificaciones
-- Sistema simplificado sin tabla teachers, incluye sistema de rating con estrellas
--
CREATE VIEW `teacher_recommendations_with_reactions` AS
SELECT 
    st.id,
    st.student_id,
    st.teacher_name,
    st.subject,
    st.semester,
    st.year,
    st.reference,
    st.rating,
    st.is_active,
    st.created_at,
    st.updated_at,
    
    -- Información del estudiante (autor de la recomendación)
    u.full_name AS student_name,
    u.email AS student_email,
    u.role AS student_role,
    
    -- Contadores de reacciones
    COUNT(DISTINCT CASE WHEN trr.reaction_type = 'LIKE' THEN trr.id END) AS like_count,
    COUNT(DISTINCT CASE WHEN trr.reaction_type = 'DISLIKE' THEN trr.id END) AS dislike_count,
    COUNT(DISTINCT trr.id) AS total_reactions
    
FROM student_teachers st
JOIN users u ON st.student_id = u.id
LEFT JOIN teacher_recommendation_reactions trr ON st.id = trr.recommendation_id
WHERE st.is_active = TRUE
GROUP BY st.id;

-- --------------------------------------------------------

--
-- Procedimientos almacenados para el sistema de recomendaciones
--

DELIMITER $

-- Procedimiento para obtener recomendaciones con reacciones del usuario
CREATE PROCEDURE `GetRecommendationsWithUserReactions`(IN user_id_param VARCHAR(36))
BEGIN
    SELECT 
        tr.*,
        -- Reacción del usuario actual (si existe)
        ur.reaction_type AS user_reaction
    FROM teacher_recommendations_with_reactions tr
    LEFT JOIN teacher_recommendation_reactions ur ON tr.id = ur.recommendation_id AND ur.user_id = user_id_param
    ORDER BY tr.created_at DESC;
END$

-- Procedimiento para obtener recomendaciones de un usuario específico
CREATE PROCEDURE `GetUserRecommendations`(IN user_id_param VARCHAR(36))
BEGIN
    SELECT * FROM teacher_recommendations_with_reactions 
    WHERE student_id = user_id_param 
    ORDER BY created_at DESC;
END$

-- Procedimiento para obtener estadísticas de recomendaciones
CREATE PROCEDURE `GetRecommendationStats`(IN user_id_param VARCHAR(36))
BEGIN
    SELECT 
        -- Estadísticas del usuario
        COUNT(DISTINCT st.id) AS my_recommendations_count,
        SUM(CASE WHEN trr.reaction_type = 'LIKE' THEN 1 ELSE 0 END) AS my_total_likes_received,
        SUM(CASE WHEN trr.reaction_type = 'DISLIKE' THEN 1 ELSE 0 END) AS my_total_dislikes_received,
        
        -- Estadísticas globales
        (SELECT COUNT(*) FROM student_teachers WHERE is_active = TRUE) AS total_recommendations_count,
        (SELECT COUNT(*) FROM teacher_recommendation_reactions) AS total_reactions_count
        
    FROM student_teachers st
    LEFT JOIN teacher_recommendation_reactions trr ON st.id = trr.recommendation_id
    WHERE st.student_id = user_id_param AND st.is_active = TRUE;
END$

DELIMITER ;

-- --------------------------------------------------------

--
-- Triggers para mantener contadores actualizados
--

DELIMITER $

CREATE TRIGGER `update_recommendation_on_reaction_insert`
AFTER INSERT ON `teacher_recommendation_reactions`
FOR EACH ROW
BEGIN
    UPDATE student_teachers 
    SET updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.recommendation_id;
END$

CREATE TRIGGER `update_recommendation_on_reaction_delete`
AFTER DELETE ON `teacher_recommendation_reactions`
FOR EACH ROW
BEGIN
    UPDATE student_teachers 
    SET updated_at = CURRENT_TIMESTAMP 
    WHERE id = OLD.recommendation_id;
END$

CREATE TRIGGER `update_recommendation_on_reaction_update`
AFTER UPDATE ON `teacher_recommendation_reactions`
FOR EACH ROW
BEGIN
    UPDATE student_teachers 
    SET updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.recommendation_id;
END$

DELIMITER ;

-- --------------------------------------------------------

--
-- Índices para la tabla `student_teachers`
-- Sistema simplificado con teacher_name y rating
--
ALTER TABLE `student_teachers`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_student_id` (`student_id`),
  ADD KEY `idx_teacher_name` (`teacher_name`),
  ADD KEY `idx_subject` (`subject`),
  ADD KEY `idx_rating` (`rating`),
  ADD KEY `idx_is_active` (`is_active`),
  ADD KEY `idx_student_teachers_active` (`is_active`, `created_at`),
  ADD KEY `idx_student_teachers_subject` (`subject`, `is_active`),
  ADD KEY `idx_teacher_name_subject` (`teacher_name`, `subject`),
  ADD KEY `idx_rating_active` (`rating`, `is_active`),
  ADD UNIQUE KEY `uk_student_teacher_subject_semester` (`student_id`, `teacher_name`(50), `subject`(50), `semester`);

--
-- Índices para la tabla `teacher_recommendation_reactions`
--
ALTER TABLE `teacher_recommendation_reactions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_recommendation` (`user_id`, `recommendation_id`),
  ADD KEY `idx_recommendation_id` (`recommendation_id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_reaction_type` (`reaction_type`),
  ADD KEY `idx_created_at` (`created_at`);

-- --------------------------------------------------------

--
-- Filtros para la tabla `student_teachers`
-- Sistema simplificado sin referencia a tabla teachers
--
ALTER TABLE `student_teachers`
  ADD CONSTRAINT `student_teachers_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `teacher_recommendation_reactions`
--
ALTER TABLE `teacher_recommendation_reactions`
  ADD CONSTRAINT `teacher_recommendation_reactions_ibfk_1` FOREIGN KEY (`recommendation_id`) REFERENCES `student_teachers` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `teacher_recommendation_reactions_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
--

--
-- Filtros para la tabla `moderation_logs`
--
ALTER TABLE `moderation_logs`
  ADD CONSTRAINT `moderation_logs_ibfk_1` FOREIGN KEY (`moderator_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;



--
-- Filtros para la tabla `posts`
--
ALTER TABLE `posts`
  ADD CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `reactions`
--
ALTER TABLE `reactions`
  ADD CONSTRAINT `reactions_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reactions_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;


COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

-- ========================================================================
-- SISTEMA DE RECOMENDACIONES DE PROFESORES - IMPLEMENTACIÓN COMPLETA CON RATING
-- ========================================================================
-- 
-- DISEÑO SIMPLIFICADO CON SISTEMA DE CALIFICACIÓN:
-- ✅ Tabla 'teachers' ELIMINADA - Sistema más simple y eficiente
-- ✅ Campo 'teacher_name' en student_teachers - Almacenamiento directo
-- ✅ Campo 'rating' con sistema de 1-5 estrellas - Calificación cuantitativa
-- ✅ Sin relaciones complejas innecesarias - Mejor rendimiento
-- ✅ Sin datos ficticios - Solo información real y útil
-- 
-- Funcionalidades incluidas:
-- ✅ Campo 'reference' en student_teachers para recomendaciones detalladas
-- ✅ Campo 'teacher_name' para almacenar nombres directamente
-- ✅ Campo 'rating' para calificación de 1-5 estrellas con validación
-- ✅ Tabla teacher_recommendation_reactions para sistema de likes/dislikes
-- ✅ Vista teacher_recommendations_with_reactions optimizada (incluye rating)
-- ✅ 3 procedimientos almacenados para consultas complejas
-- ✅ 3 triggers para auditoría automática de timestamps
-- ✅ Índices optimizados para rendimiento máximo (incluye índice de rating)
-- ✅ Constraints de integridad referencial y validación de rating (1-5)
-- 
-- SISTEMA DE CALIFICACIÓN CON ESTRELLAS:
-- ✅ Rating obligatorio para nuevas recomendaciones (1-5 estrellas)
-- ✅ Compatibilidad con recomendaciones legacy (rating NULL permitido)
-- ✅ Constraint CHECK para validar rango de rating
-- ✅ Índice optimizado para consultas por rating
-- ✅ Visualización: "⭐⭐⭐⭐⭐ (5/5)" o "⭐ Sin calificación"
-- 
-- BENEFICIOS DE LA IMPLEMENTACIÓN:
-- ✅ Consultas más rápidas (menos JOINs)
-- ✅ Código más simple y mantenible
-- ✅ Información cuantitativa (estrellas) + cualitativa (referencia)
-- ✅ Sistema familiar e intuitivo para usuarios
-- ✅ Mejor toma de decisiones para estudiantes
-- ✅ Métricas mejoradas para análisis institucional
-- 
-- APIs disponibles después de ejecutar este script:
-- GET    /teacher-recommendations                       - Obtener todas las recomendaciones
-- GET    /teacher-recommendations?subject={materia}     - Filtrar por materia
-- GET    /teacher-recommendations?userId={id}           - Con estado del usuario
-- GET    /teacher-recommendations/user/{userId}         - Recomendaciones propias
-- POST   /teacher-recommendations                       - Crear recomendación (legacy)
-- POST   /teacher-recommendations/with-rating           - Crear recomendación con rating ⭐
-- POST   /teacher-recommendations/{id}/like             - Dar/quitar like
-- POST   /teacher-recommendations/{id}/dislike          - Dar/quitar dislike
-- DELETE /teacher-recommendations/{id}                  - Eliminar recomendación
-- GET    /teacher-recommendations/stats/{userId}        - Estadísticas del usuario
-- GET    /teacher-recommendations/subjects              - Materias disponibles
-- 
-- Para activar el sistema completo con rating:
-- 1. Ejecutar este script: mysql -u root -p uniway_db < database/uniway_db.sql
-- 2. Compilar backend: mvn clean compile && mvn spring-boot:run
-- 3. Compilar frontend: ./gradlew assembleDebug
-- 4. Probar funcionalidad: Crear recomendación con estrellas en la app
-- 
-- Ejemplo de uso del nuevo endpoint con rating:
-- POST /teacher-recommendations/with-rating
-- {
--   "studentId": "user-123",
--   "teacherName": "María González",
--   "subject": "Programación I", 
--   "semester": "2024-2",
--   "year": 2024,
--   "reference": "Excelente profesora, muy clara...",
--   "rating": 5
-- }
-- 
-- Última actualización: Sistema Completo con Calificación por Estrellas (1-5)
-- ========================================================================
