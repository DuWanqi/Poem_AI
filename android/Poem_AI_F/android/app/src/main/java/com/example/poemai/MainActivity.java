import com.example.poemai.model.ApiResponse; // 确保正确导入 ApiResponse

public class MainActivity extends AppCompatActivity {

    private void loadWorks() {
        String token = preferencesManager.getToken();
        if (token == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 调用 API 获取作品列表
        Call<ApiResponse> call = RetrofitClient.getInstance().getApiService().getAllWorks("Bearer " + token);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    worksList.clear();
                    worksList.addAll(response.body().getData());
                    workAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "加载作品列表失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}