@GET("api/work/")
Call<ApiResponse> getAllWorks(@Header("Authorization") String token);