package com.uniway.entity;

/**
 * Enum ReactionType - Define los tipos de reacciones disponibles en el sistema
 * 
 * Este enum se usa en la entidad Reaction para especificar el tipo de reacción
 * que un usuario puede dar a un post.
 * 
 * Valores disponibles:
 * - LIKE: Reacción positiva (me gusta)
 * - DISLIKE: Reacción negativa (no me gusta)
 * 
 * El sistema implementa lógica de toggle:
 * - Si el usuario da LIKE y ya tenía LIKE, se elimina la reacción
 * - Si el usuario da LIKE y tenía DISLIKE, cambia a LIKE
 * - Si el usuario da DISLIKE y ya tenía DISLIKE, se elimina la reacción
 * - Si el usuario da DISLIKE y tenía LIKE, cambia a DISLIKE
 */
public enum ReactionType {
    /** Reacción positiva - indica que al usuario le gusta el post */
    LIKE,
    
    /** Reacción negativa - indica que al usuario no le gusta el post */
    DISLIKE
}