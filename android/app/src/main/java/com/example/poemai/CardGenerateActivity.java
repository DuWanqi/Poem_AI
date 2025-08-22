package com.example.poemai;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.poemai.utils.PreferencesManager;
import com.example.poemai.service.BackendService;
import com.example.poemai.model.WorkSaveResponse;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardGenerateActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1001;
    private static final int REQUEST_CODE_PICK_IMAGE = 1002;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1003;
    
    private TextView tvCardPreview; // 修改为TextView而不是VerticalTextView
    private Button btnSelectBackground, btnSaveToLocal, btnSaveToWorks, btnClose;
    private SharedPreferences settingsPrefs;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_generate);
        
        settingsPrefs = getSharedPreferences("PoemComposeSettings", MODE_PRIVATE);
        preferencesManager = new PreferencesManager(this);
        
        // 清理旧的背景图片（保留当前使用的）
        cleanupOldBackgrounds();
        
        initViews();
        setupListeners();
        loadPreviewContent();
    }
    
    private void cleanupOldBackgrounds() {
        try {
            String currentBackground = settingsPrefs.getString("selected_background", "blank");
            String currentBackgroundPath = settingsPrefs.getString("custom_background_path", "");
            
            // 如果当前背景不是自定义图片，则清理所有自定义背景图片
            if (!"custom_image".equals(currentBackground)) {
                File backgroundsDir = new File(getFilesDir(), "backgrounds");
                if (backgroundsDir.exists() && backgroundsDir.isDirectory()) {
                    File[] files = backgroundsDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            file.delete();
                        }
                    }
                }
                
                // 清理SharedPreferences中的自定义背景记录
                SharedPreferences.Editor editor = settingsPrefs.edit();
                editor.remove("custom_background_path");
                editor.remove("custom_background_uri");
                editor.apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void cleanupCustomBackground() {
        try {
            String customBackgroundPath = settingsPrefs.getString("custom_background_path", "");
            if (!customBackgroundPath.isEmpty()) {
                File imageFile = new File(customBackgroundPath);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
                
                // 清理SharedPreferences中的自定义背景记录
                SharedPreferences.Editor editor = settingsPrefs.edit();
                editor.remove("custom_background_path");
                editor.remove("custom_background_uri");
                editor.apply();
            }
            
            // 清理背景图片目录中的所有文件
            File backgroundsDir = new File(getFilesDir(), "backgrounds");
            if (backgroundsDir.exists() && backgroundsDir.isDirectory()) {
                File[] files = backgroundsDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initViews() {
        tvCardPreview = findViewById(R.id.tvCardPreview); // 现在不需要强制类型转换
        btnSelectBackground = findViewById(R.id.btnSelectBackground);
        btnSaveToLocal = findViewById(R.id.btnSaveToLocal);
        btnSaveToWorks = findViewById(R.id.btnSaveToWorks);
        btnClose = findViewById(R.id.btnBack); // 修复按钮ID引用
    }
    
    private void setupListeners() {
        btnSelectBackground.setOnClickListener(v -> showBackgroundSelectDialog());
        btnSaveToLocal.setOnClickListener(v -> saveCardToLocal());
        btnSaveToWorks.setOnClickListener(v -> saveWorkToMyWorks());
        btnClose.setOnClickListener(v -> finish());
    }
    
    private void loadPreviewContent() {
        // 获取传递的内容
        String content = getIntent().getStringExtra("content");
        if (content != null) {
            tvCardPreview.setText(content);
        }
        
        // 应用保存的背景
        applySelectedBackground();
    }
    
    private void applySelectedBackground() {
        String selectedBackground = settingsPrefs.getString("selected_background", "blank");
        switch (selectedBackground) {
            case "elegant":
                tvCardPreview.setBackgroundResource(R.drawable.elegant_background);
                break;
            case "heroic":
                tvCardPreview.setBackgroundResource(R.drawable.heroic_background);
                break;
            case "blank":
                tvCardPreview.setBackgroundResource(R.drawable.blank_background);
                break;
            case "red_solid":
                tvCardPreview.setBackgroundResource(R.drawable.red_solid_background);
                break;
            case "dark_solid":
                tvCardPreview.setBackgroundResource(R.drawable.dark_solid_background);
                break;
            case "custom_image":
                String customBackgroundPath = settingsPrefs.getString("custom_background_path", "");
                if (!customBackgroundPath.isEmpty()) {
                    try {
                        File imageFile = new File(customBackgroundPath);
                        if (imageFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(customBackgroundPath);
                            if (bitmap != null) {
                                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                                tvCardPreview.setBackground(drawable);
                            } else {
                                tvCardPreview.setBackgroundResource(R.drawable.blank_background);
                            }
                        } else {
                            // 文件不存在，清理SharedPreferences中的记录
                            SharedPreferences.Editor editor = settingsPrefs.edit();
                            editor.remove("selected_background");
                            editor.remove("custom_background_path");
                            editor.apply();
                            tvCardPreview.setBackgroundResource(R.drawable.blank_background);
                        }
                    } catch (Exception e) {
                        tvCardPreview.setBackgroundResource(R.drawable.blank_background);
                    }
                } else {
                    tvCardPreview.setBackgroundResource(R.drawable.blank_background);
                }
                break;
        }
    }
    
    private void showBackgroundSelectDialog() {
        Dialog backgroundDialog = new Dialog(this);
        backgroundDialog.setContentView(R.layout.dialog_background_select);
        
        // 初始化控件
        Button btnBlank = backgroundDialog.findViewById(R.id.btnBlank);
        Button btnRedSolid = backgroundDialog.findViewById(R.id.btnRedSolid);
        Button btnDarkSolid = backgroundDialog.findViewById(R.id.btnDarkSolid);
        Button btnImportBackground = backgroundDialog.findViewById(R.id.btnImportBackground);
        Button btnCloseBackground = backgroundDialog.findViewById(R.id.btnCloseBackground);
        
        // 设置按钮点击事件
        btnBlank.setOnClickListener(v -> {
            Toast.makeText(this, "已选择空白风格背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "blank");
            // 清理自定义背景图片
            cleanupCustomBackground();
            editor.apply();
            applySelectedBackground();
            backgroundDialog.dismiss();
        });
        
        btnRedSolid.setOnClickListener(v -> {
            Toast.makeText(this, "已选择深红纯色背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "red_solid");
            // 清理自定义背景图片
            cleanupCustomBackground();
            editor.apply();
            applySelectedBackground();
            backgroundDialog.dismiss();
        });
        
        btnDarkSolid.setOnClickListener(v -> {
            Toast.makeText(this, "已选择淡黑纯色背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "dark_solid");
            // 清理自定义背景图片
            cleanupCustomBackground();
            editor.apply();
            applySelectedBackground();
            backgroundDialog.dismiss();
        });
        
        btnImportBackground.setOnClickListener(v -> {
            // 直接尝试打开图片选择器，让系统处理权限问题
            openImagePickerWithFallback();
            backgroundDialog.dismiss();
        });
        
        btnCloseBackground.setOnClickListener(v -> backgroundDialog.dismiss());
        
        backgroundDialog.show();
    }
    
    private void saveWorkToMyWorks() {
        // 获取预览内容
        String content = tvCardPreview.getText().toString();
        
        // 创建作品数据
        Map<String, Object> workData = new HashMap<>();
        workData.put("title", "我的作品");
        workData.put("content", content);
        workData.put("workType", "template_poem");
        
        // 添加字体设置
        Map<String, Object> fontSetting = new HashMap<>();
        fontSetting.put("font", "默认");
        fontSetting.put("size", 16);
        fontSetting.put("color", "#000000");
        workData.put("fontSetting", fontSetting);
        
        // 添加背景信息 (从SharedPreferences获取)
        Map<String, Object> backgroundInfo = new HashMap<>();
        String selectedBackground = settingsPrefs.getString("selected_background", "blank");
        backgroundInfo.put("type", "drawable");
        backgroundInfo.put("value", selectedBackground);
        workData.put("backgroundInfo", backgroundInfo);
        
        // 获取用户token
        String token = preferencesManager.getToken();
        long userId = preferencesManager.getUserId();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 使用BackendService保存作品
        BackendService backendService = BackendService.getInstance(this);
        BackendService.Result<Map<String, Object>> result = backendService.saveWork(workData, userId);
        
        if (result.getCode() == 200) {
            Toast.makeText(CardGenerateActivity.this, "作品保存成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(CardGenerateActivity.this, "作品保存失败: " + result.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    // 将选中的图片复制到应用私有存储中
                    String savedImagePath = copyImageToPrivateStorage(selectedImageUri);
                    if (savedImagePath != null) {
                        // 保存图片路径到SharedPreferences
                        SharedPreferences.Editor editor = settingsPrefs.edit();
                        editor.putString("selected_background", "custom_image");
                        editor.putString("custom_background_path", savedImagePath);
                        editor.remove("custom_background_uri"); // 移除旧的URI
                        editor.apply();
                        
                        // 应用选中的背景
                        applySelectedBackground();
                        
                        Toast.makeText(this, "已选择自定义图片背景", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "保存背景图片失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "设置背景图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private String copyImageToPrivateStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;
            
            // 创建文件名
            String fileName = "custom_background_" + System.currentTimeMillis() + ".jpg";
            
            // 获取应用私有目录
            File privateDir = new File(getFilesDir(), "backgrounds");
            if (!privateDir.exists()) {
                privateDir.mkdirs();
            }
            
            // 创建目标文件
            File targetFile = new File(privateDir, fileName);
            
            // 复制文件
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            inputStream.close();
            outputStream.close();
            
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予，正在打开图片选择器...", Toast.LENGTH_SHORT).show();
                openImagePicker();
            } else {
                // 检查是否用户选择了"不再询问"
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionDeniedDialog();
                } else {
                    Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performSaveCardToLocal();
            } else {
                Toast.makeText(this, "需要存储权限才能保存图片到本地", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_CODE_PICK_IMAGE);
    }
    
    private void openImagePickerWithFallback() {
        try {
            openImagePicker();
        } catch (Exception e) {
            // 如果直接打开图片选择器失败，则尝试请求权限
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionExplanationDialog(() -> {
                        ActivityCompat.requestPermissions(this, 
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                            REQUEST_CODE_READ_EXTERNAL_STORAGE);
                    });
                } else {
                    showPermissionDeniedDialog();
                }
            }
        }
    }
    
    private void saveCardToLocal() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13及以上版本
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
                // 检查是否应该显示权限请求的解释
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    // 显示解释为什么需要权限，然后请求权限
                    showPermissionExplanationDialog(() -> {
                        ActivityCompat.requestPermissions(this, 
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                            REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                    });
                } else {
                    // 权限被永久拒绝或首次请求权限
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                }
            } else {
                performSaveCardToLocal();
            }
        } else {
            // Android 12及以下版本
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                // 检查是否应该显示权限请求的解释
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 显示解释为什么需要权限，然后请求权限
                    showPermissionExplanationDialog(() -> {
                        ActivityCompat.requestPermissions(this, 
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                            REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                    });
                } else {
                    // 权限被永久拒绝或首次请求权限
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                }
            } else {
                performSaveCardToLocal();
            }
        }
    }
    
    private void performSaveCardToLocal() {
        try {
            // 创建Bitmap
            Bitmap bitmap = createBitmapFromView(tvCardPreview);
            
            // 保存Bitmap到本地
            String fileName = "poem_card_" + System.currentTimeMillis() + ".png";
            boolean saved = saveImageToGallery(bitmap, fileName);
            
            if (saved) {
                Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存过程中出现错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private Bitmap createBitmapFromView(TextView view) {
        // 创建与TextView相同尺寸的Bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // 绘制背景
        Drawable background = view.getBackground();
        if (background != null) {
            background.draw(canvas);
        } else {
            // 如果没有背景，绘制默认背景
            canvas.drawColor(Color.WHITE);
        }
        
        // 绘制文字内容
        view.draw(canvas);
        
        return bitmap;
    }
    
    private boolean saveImageToGallery(Bitmap bitmap, String fileName) {
        try {
            // 获取外部存储的图片目录
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(picturesDir, fileName);
            
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            
            // 通知媒体扫描器有新图片
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(imageFile);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void showPermissionExplanationDialog(Runnable onConfirm) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("需要存储权限")
               .setMessage("此功能需要访问设备存储权限以保存图片到本地。请在接下来的系统权限请求对话框中授予权限。")
               .setPositiveButton("继续", (dialog, which) -> onConfirm.run())
               .setNegativeButton("取消", null)
               .show();
    }
    
    private void showPermissionDeniedDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("权限被拒绝")
               .setMessage("存储权限已被拒绝。请手动到系统设置中为本应用授予权限，或者您可以选择其他背景选项。")
               .setPositiveButton("前往设置", (dialog, which) -> openAppSettings())
               .setNegativeButton("选择其他背景", null)
               .show();
    }
    
    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}