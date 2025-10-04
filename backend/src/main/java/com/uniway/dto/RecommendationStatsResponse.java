package com.uniway.dto;

/**
 * RecommendationStatsResponse - DTO para estadísticas de recomendaciones
 * 
 * Contiene información estadística sobre las recomendaciones de un usuario
 * incluyendo contadores personales y globales del sistema.
 */
public class RecommendationStatsResponse {
    
    private int myRecommendationsCount;
    private int myTotalLikesReceived;
    private int myTotalDislikesReceived;
    private int totalRecommendationsCount;
    private int totalReactionsCount;

    // Constructor
    public RecommendationStatsResponse(int myRecommendationsCount, int myTotalLikesReceived, 
                                     int myTotalDislikesReceived, int totalRecommendationsCount, 
                                     int totalReactionsCount) {
        this.myRecommendationsCount = myRecommendationsCount;
        this.myTotalLikesReceived = myTotalLikesReceived;
        this.myTotalDislikesReceived = myTotalDislikesReceived;
        this.totalRecommendationsCount = totalRecommendationsCount;
        this.totalReactionsCount = totalReactionsCount;
    }

    // Constructor vacío
    public RecommendationStatsResponse() {}

    // Getters y Setters
    public int getMyRecommendationsCount() { return myRecommendationsCount; }
    public void setMyRecommendationsCount(int myRecommendationsCount) { this.myRecommendationsCount = myRecommendationsCount; }

    public int getMyTotalLikesReceived() { return myTotalLikesReceived; }
    public void setMyTotalLikesReceived(int myTotalLikesReceived) { this.myTotalLikesReceived = myTotalLikesReceived; }

    public int getMyTotalDislikesReceived() { return myTotalDislikesReceived; }
    public void setMyTotalDislikesReceived(int myTotalDislikesReceived) { this.myTotalDislikesReceived = myTotalDislikesReceived; }

    public int getTotalRecommendationsCount() { return totalRecommendationsCount; }
    public void setTotalRecommendationsCount(int totalRecommendationsCount) { this.totalRecommendationsCount = totalRecommendationsCount; }

    public int getTotalReactionsCount() { return totalReactionsCount; }
    public void setTotalReactionsCount(int totalReactionsCount) { this.totalReactionsCount = totalReactionsCount; }
}