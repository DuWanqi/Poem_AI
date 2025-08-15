import com.example.poemai.model.ApiResponse;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkAdapter workAdapter;
    private List<Work> worksList;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.recyclerView);
        worksList = new ArrayList<>();
        workAdapter = new WorkAdapter(worksList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(workAdapter);

        preferencesManager = new PreferencesManager(this);

        loadWorks();
    }

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
                    Toast.makeText(GalleryActivity.this, "加载作品列表失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(GalleryActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
