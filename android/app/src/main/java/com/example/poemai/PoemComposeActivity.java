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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.example.poemai.model.RhymeResponse;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import com.example.poemai.service.BackendService;
import com.example.poemai.utils.PreferencesManager;
import com.example.poemai.model.CiPai;
import com.example.poemai.model.WorkSaveResponse;
import com.example.poemai.network.RetrofitClient;
import com.example.poemai.model.RhymeResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PoemComposeActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1001;
    private static final int REQUEST_CODE_PICK_IMAGE = 1002;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1003;
    
    private static final String PREFS_NAME = "PoemComposeSettings";
    private static final String FONT_SIZE_KEY = "font_size";
    private static final String TEXT_COLOR_KEY = "text_color";
    private static final String TEXT_DIRECTION_KEY = "text_direction";
    private static final String FONT_FAMILY_KEY = "font_family";
    
    // 横排模式下的控件
    private VerticalTextView etPoemContent;
    private EditText etTitle, etAuthor;
    
    // 竖排模式下的控件
    private VerticalTextView etPoemContentVertical, etTitleVertical, etAuthorVertical;
    
    private TextView tvCiPaiExample;
    private ImageButton btnRhyme, btnShare, btnSettings, btnInspiration, btnBack, btnHome;
    private LinearLayout layoutHorizontal, layoutVertical;
    private PreferencesManager preferencesManager;
    private CiPai selectedCiPai;
    private SharedPreferences settingsPrefs;
    
    // 默认设置值
    private int currentFontSize = 16;
    private int currentTextColor = R.color.classical_text;
    private int currentTextDirection = 0; // 0 for horizontal, 1 for vertical
    private int currentFontFamily = 0; // 0 for default, 1 for 宋体(Serif), 2 for 黑体(Sans Serif), 3 for 等宽字体(Monospace), 4 for 仿宋(FangSong), 5 for 楷体(KaiTi)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 应用古典风格主题
        setTheme(R.style.Theme_PoemAI_Classical);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_compose);

        preferencesManager = new PreferencesManager(this);
        settingsPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // 清理旧的背景图片（保留当前使用的）
        cleanupOldBackgrounds();
        
        // 加载保存的设置
        loadSettings();
        
        initViews();
        loadCardContent();
        setupListeners();
        applySettings();
    }

    private void loadSettings() {
        currentFontSize = settingsPrefs.getInt(FONT_SIZE_KEY, 16);
        currentTextColor = settingsPrefs.getInt(TEXT_COLOR_KEY, R.color.classical_text);
        currentTextDirection = settingsPrefs.getInt(TEXT_DIRECTION_KEY, 0);
        currentFontFamily = settingsPrefs.getInt(FONT_FAMILY_KEY, 0);
    }
    
    /**
     * 清理旧的背景图片（保留当前使用的）
     * 如果当前背景不是自定义图片，则清理所有自定义背景图片
     */
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
    
    private void saveSettings() {
        SharedPreferences.Editor editor = settingsPrefs.edit();
        editor.putInt(FONT_SIZE_KEY, currentFontSize);
        editor.putInt(TEXT_COLOR_KEY, currentTextColor);
        editor.putInt(TEXT_DIRECTION_KEY, currentTextDirection);
        editor.putInt(FONT_FAMILY_KEY, currentFontFamily);
        editor.apply();
    }
    
    private void applySettings() {
        // 应用字体大小
        etPoemContent.setTextSize(currentFontSize);
        etTitle.setTextSize(currentFontSize);
        etAuthor.setTextSize(currentFontSize);
        etPoemContentVertical.setTextSize(currentFontSize);
        etTitleVertical.setTextSize(currentFontSize);
        etAuthorVertical.setTextSize(currentFontSize);
        
        // 应用文本颜色
        int color = ContextCompat.getColor(this, currentTextColor);
        etPoemContent.setTextColor(color);
        etTitle.setTextColor(color);
        etAuthor.setTextColor(color);
        etPoemContentVertical.setTextColor(color);
        etTitleVertical.setTextColor(color);
        etAuthorVertical.setTextColor(color);
        
        // 应用文本方向
        applyTextDirection();
        
        // 应用字体
        applyFontFamily();
    }
    
    private void applyFontFamily() {
        android.graphics.Typeface typeface = android.graphics.Typeface.DEFAULT;
        switch (currentFontFamily) {
            case 0: // 默认系统字体
                typeface = android.graphics.Typeface.DEFAULT;
                break;
            case 1: // 宋体（Serif）
                typeface = android.graphics.Typeface.SERIF;
                break;
            case 2: // 黑体（Sans Serif）
                typeface = android.graphics.Typeface.SANS_SERIF;
                break;
            case 3: // 等宽字体
                typeface = android.graphics.Typeface.MONOSPACE;
                break;
            case 4: // 仿宋字体（如果可用）
                typeface = android.graphics.Typeface.create("fangsong", android.graphics.Typeface.NORMAL);
                break;
            case 5: // 楷体字体（如果可用）
                typeface = android.graphics.Typeface.create("kaiti", android.graphics.Typeface.NORMAL);
                break;
        }
        
        etPoemContent.setTypeface(typeface);
        etTitle.setTypeface(typeface);
        etAuthor.setTypeface(typeface);
        etPoemContentVertical.setTypeface(typeface);
        etTitleVertical.setTypeface(typeface);
        etAuthorVertical.setTypeface(typeface);
    }
    
    private void applyTextDirection() {
        // 横排
        if (currentTextDirection == 0) {
            layoutHorizontal.setVisibility(View.VISIBLE);
            layoutVertical.setVisibility(View.GONE);
            etPoemContent.setVertical(false);
        } 
        // 竖排
        else {
            layoutHorizontal.setVisibility(View.GONE);
            layoutVertical.setVisibility(View.VISIBLE);
            etPoemContentVertical.setVertical(true);
            etTitleVertical.setVertical(true);
            etAuthorVertical.setVertical(true);
            
            // 同步文本内容
            syncTextContent();
        }
    }
    
    private void syncTextContent() {
        // 同步内容到竖排模式
        if (currentTextDirection == 1) {
            etPoemContentVertical.setText(etPoemContent.getText());
            etTitleVertical.setText(etTitle.getText());
            etAuthorVertical.setText(etAuthor.getText());
        }
    }

    private void initViews() {
        // 初始化横排模式控件
        etPoemContent = findViewById(R.id.etPoemContent);
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        
        // 初始化竖排模式控件
        etPoemContentVertical = findViewById(R.id.etPoemContentVertical);
        etTitleVertical = findViewById(R.id.etTitleVertical);
        etAuthorVertical = findViewById(R.id.etAuthorVertical);
        
        // 布局容器
        layoutHorizontal = findViewById(R.id.layoutHorizontal);
        layoutVertical = findViewById(R.id.layoutVertical);
        
        tvCiPaiExample = findViewById(R.id.tvCiPaiExample);
        
        btnRhyme = findViewById(R.id.btnRhyme);
        btnShare = findViewById(R.id.btnShare);
        btnSettings = findViewById(R.id.btnSettings);
        btnInspiration = findViewById(R.id.btnInspiration);
        btnBack = findViewById(R.id.btnBack);
        btnHome = findViewById(R.id.btnHome);
    }

    private void loadCardContent() {
        String cardContent = getIntent().getStringExtra("card_content");
        if (cardContent != null) {
            etPoemContent.setText(cardContent);
            etPoemContentVertical.setText(cardContent);
        }
        
        // 获取选中的词牌信息
        selectedCiPai = (CiPai) getIntent().getSerializableExtra("selected_cipai");
        if (selectedCiPai != null) {
            // 显示词牌示例
            if (selectedCiPai.getExampleText() != null) {
                tvCiPaiExample.setText(selectedCiPai.getExampleText());
            }
        }
    }

    private void setupListeners() {
        btnRhyme.setOnClickListener(v -> showRhymeDialog());
        btnShare.setOnClickListener(v -> showShareDialog());
        btnSettings.setOnClickListener(v -> showSettingsDialog());
        btnInspiration.setOnClickListener(v -> showInspirationDialog());
        btnBack.setOnClickListener(v -> finish()); // 添加返回按钮点击事件
        btnHome.setOnClickListener(v -> {
            // 跳转到主页面
            Intent intent = new Intent(PoemComposeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }); // 添加Home按钮点击事件
        
        // 添加文本变化监听器以同步横竖排内容
        etPoemContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (currentTextDirection == 1) {
                    etPoemContentVertical.setText(s);
                }
            }
        });
        
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (currentTextDirection == 1) {
                    etTitleVertical.setText(s);
                }
            }
        });
        
        etAuthor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (currentTextDirection == 1) {
                    etAuthorVertical.setText(s);
                }
            }
        });
    }

    private void showRhymeDialog() {
        Dialog rhymeDialog = new Dialog(this);
        rhymeDialog.setContentView(R.layout.dialog_rhyme);
        
        // 初始化控件
        EditText etRhymeSearch = rhymeDialog.findViewById(R.id.etRhymeSearch);
        TextView tvRhymeResults = rhymeDialog.findViewById(R.id.tvRhymeResults);
        Button btnCloseRhyme = rhymeDialog.findViewById(R.id.btnCloseRhyme);
        
        // 设置搜索框文本变化监听器
        etRhymeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    searchRhymeWords(query, tvRhymeResults);
                }
            }
        });
        
        // 设置关闭按钮点击事件
        btnCloseRhyme.setOnClickListener(v -> rhymeDialog.dismiss());
        
        rhymeDialog.show();
    }

    private void searchRhymeWords(String query, TextView tvRhymeResults) {
        // 使用本地BackendService进行押韵查询
        BackendService backendService = BackendService.getInstance(this);
        BackendService.Result<Map<String, Object>> result = backendService.getRhymeInfoByChar(query);
        
        if (result.getCode() == 200 && result.getData() != null) {
            Map<String, Object> data = result.getData();
            String rhymeGroup = (String) data.get("rhymeGroup");
            Object wordsObj = data.get("words");
            
            // 正确处理从BackendService返回的押韵字列表
            List<String> words = new ArrayList<>();
            if (wordsObj instanceof List) {
                // 如果是List类型，直接转换
                words = (List<String>) wordsObj;
            } else if (wordsObj instanceof String[]) {
                // 如果是String[]类型，转换为List
                String[] wordsArray = (String[]) wordsObj;
                words = Arrays.asList(wordsArray);
            }
            
            if (words != null && !words.isEmpty()) {
                StringBuilder resultText = new StringBuilder();
                resultText.append("韵脚字（韵母组: ").append(rhymeGroup).append("）:\n\n");
                for (String word : words) {
                    resultText.append(word).append("  ");
                }
                tvRhymeResults.setText(resultText.toString());
            } else {
                tvRhymeResults.setText("未找到相关韵脚字");
            }
        } else {
            tvRhymeResults.setText("查询失败: " + result.getMessage());
        }
    }

    private void showShareDialog() {
        Dialog shareDialog = new Dialog(this);
        shareDialog.setContentView(R.layout.dialog_share);
        
        // 初始化控件
        TextView tvSharePreview = shareDialog.findViewById(R.id.tvSharePreview);
        Button btnSelectBackground = shareDialog.findViewById(R.id.btnSelectBackground);
        Button btnSaveToLocal = shareDialog.findViewById(R.id.btnSaveToLocal);
        Button btnSaveToWorks = shareDialog.findViewById(R.id.btnSaveToWorks);
        Button btnCloseShare = shareDialog.findViewById(R.id.btnCloseShare);
        
        // 设置预览文本
        String title = etTitle.getText().toString();
        String author = etAuthor.getText().toString();
        String content = etPoemContent.getText().toString();
        
        String previewText = "";
        if (!title.isEmpty()) {
            previewText += "《" + title + "》\n";
        }
        if (!author.isEmpty()) {
            previewText += "作者：" + author + "\n\n";
        }
        previewText += content;
        
        tvSharePreview.setText(previewText);
        
        // 根据保存的背景设置预览背景
        String selectedBackground = settingsPrefs.getString("selected_background", "blank");
        switch (selectedBackground) {
            case "blank":
                tvSharePreview.setBackgroundResource(R.drawable.blank_background);
                break;
            case "red_solid":
                tvSharePreview.setBackgroundResource(R.drawable.red_solid_background);
                break;
            case "dark_solid":
                tvSharePreview.setBackgroundResource(R.drawable.dark_solid_background);
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
                                tvSharePreview.setBackground(drawable);
                            } else {
                                tvSharePreview.setBackgroundResource(R.drawable.blank_background);
                            }
                        } else {
                            // 文件不存在，清理SharedPreferences中的记录
                            SharedPreferences.Editor editor = settingsPrefs.edit();
                            editor.remove("selected_background");
                            editor.remove("custom_background_path");
                            editor.apply();
                            tvSharePreview.setBackgroundResource(R.drawable.blank_background);
                        }
                    } catch (Exception e) {
                        tvSharePreview.setBackgroundResource(R.drawable.blank_background);
                    }
                } else {
                    tvSharePreview.setBackgroundResource(R.drawable.blank_background);
                }
                break;
        }
        
        // 设置按钮点击事件
        btnSelectBackground.setOnClickListener(v -> {
            // 显示背景选择对话框
            showBackgroundSelectDialog(shareDialog);
        });
        
        btnSaveToLocal.setOnClickListener(v -> {
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
                    performSaveCardToLocal(tvSharePreview);
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
                    performSaveCardToLocal(tvSharePreview);
                }
            }
        });
        
        btnSaveToWorks.setOnClickListener(v -> {
            // 保存作品到我的作品
            saveWorkToMyWorks(title, author, content, selectedBackground);
        });
        
        btnCloseShare.setOnClickListener(v -> shareDialog.dismiss());
        
        shareDialog.show();
    }
    
    private void showBackgroundSelectDialog(Dialog parentDialog) {
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
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_CODE_PICK_IMAGE);
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
                // 用户永久拒绝权限（勾选了"不再询问"）
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionDeniedDialog();
                } else {
                    Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            boolean permissionGranted = false;
            
            // 检查是否所有请求的权限都被授予
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    break;
                }
            }
            
            if (permissionGranted) {
                // 重新显示分享对话框并执行保存操作
                showShareDialog();
            } else {
                Toast.makeText(this, "需要存储权限才能保存图片到本地", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void showSettingsDialog() {
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_settings);
        
        // 初始化设置对话框中的控件
        Button btnFontSizeDecrease = settingsDialog.findViewById(R.id.btnFontSizeDecrease);
        Button btnFontSizeIncrease = settingsDialog.findViewById(R.id.btnFontSizeIncrease);
        TextView tvFontSize = settingsDialog.findViewById(R.id.tvFontSize);
        Button btnImportFont = settingsDialog.findViewById(R.id.btnImportFont);
        Button btnSelectDirection = settingsDialog.findViewById(R.id.btnSelectDirection);
        Button btnCloseSettings = settingsDialog.findViewById(R.id.btnCloseSettings);
        
        // 设置当前字体大小显示
        tvFontSize.setText(String.valueOf(currentFontSize));
        
        // 设置按钮点击事件
        btnFontSizeDecrease.setOnClickListener(v -> {
            if (currentFontSize > 8) {
                currentFontSize--;
                tvFontSize.setText(String.valueOf(currentFontSize));
                Toast.makeText(this, "字体大小: " + currentFontSize, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "已达到最小字体大小", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnFontSizeIncrease.setOnClickListener(v -> {
            if (currentFontSize < 36) {
                currentFontSize++;
                tvFontSize.setText(String.valueOf(currentFontSize));
                Toast.makeText(this, "字体大小: " + currentFontSize, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "已达到最大字体大小", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnImportFont.setOnClickListener(v -> {
            // 显示字体选择对话框
            showFontSelectDialog();
        });
        
        btnSelectDirection.setOnClickListener(v -> {
            // 选择排列方向
            showDirectionSelectDialog();
        });
        
        btnCloseSettings.setOnClickListener(v -> {
            settingsDialog.dismiss();
            // 保存设置
            saveSettings();
            // 应用设置
            applySettings();
        });
        
        // 字体颜色选择按钮点击事件
        View.OnClickListener colorClickListener = v -> {
            if (v == settingsDialog.findViewById(R.id.btnColorBlack)) {
                currentTextColor = android.R.color.black;
                Toast.makeText(this, "已选择黑色字体", Toast.LENGTH_SHORT).show();
            } else if (v == settingsDialog.findViewById(R.id.btnColorWhite)) {
                currentTextColor = android.R.color.white;
                Toast.makeText(this, "已选择白色字体", Toast.LENGTH_SHORT).show();
            } else if (v == settingsDialog.findViewById(R.id.btnColorGold)) {
                currentTextColor = R.color.gold;
                Toast.makeText(this, "已选择金色字体", Toast.LENGTH_SHORT).show();
            } else if (v == settingsDialog.findViewById(R.id.btnColorDarkRed)) {
                currentTextColor = R.color.dark_red;
                Toast.makeText(this, "已选择深红色字体", Toast.LENGTH_SHORT).show();
            }
        };
        
        // 为字体颜色按钮设置点击事件
        settingsDialog.findViewById(R.id.btnColorBlack).setOnClickListener(colorClickListener);
        settingsDialog.findViewById(R.id.btnColorWhite).setOnClickListener(colorClickListener);
        settingsDialog.findViewById(R.id.btnColorGold).setOnClickListener(colorClickListener);
        settingsDialog.findViewById(R.id.btnColorDarkRed).setOnClickListener(colorClickListener);
        
        settingsDialog.show();
    }
    
    private void showFontSelectDialog() {
        // 创建字体选择对话框
        Dialog fontDialog = new Dialog(this);
        fontDialog.setContentView(R.layout.dialog_font_select);
        
        // 获取对话框中的控件
        Button btnDefault = fontDialog.findViewById(R.id.btnDefault);
        Button btnSerif = fontDialog.findViewById(R.id.btnSerif);
        Button btnSansSerif = fontDialog.findViewById(R.id.btnSansSerif);
        Button btnMonospace = fontDialog.findViewById(R.id.btnMonospace);
        Button btnFangSong = fontDialog.findViewById(R.id.btnFangSong);
        Button btnKaiTi = fontDialog.findViewById(R.id.btnKaiTi);
        Button btnClose = fontDialog.findViewById(R.id.btnCloseFont);
        
        // 设置按钮点击事件
        btnDefault.setOnClickListener(v -> {
            currentFontFamily = 0;
            Toast.makeText(this, "已选择系统默认字体", Toast.LENGTH_SHORT).show();
            fontDialog.dismiss();
        });
        
        btnSerif.setOnClickListener(v -> {
            currentFontFamily = 1;
            Toast.makeText(this, "已选择宋体", Toast.LENGTH_SHORT).show();
            fontDialog.dismiss();
        });
        
        btnSansSerif.setOnClickListener(v -> {
            currentFontFamily = 2;
            Toast.makeText(this, "已选择黑体", Toast.LENGTH_SHORT).show();
            fontDialog.dismiss();
        });
        
        btnMonospace.setOnClickListener(v -> {
            currentFontFamily = 3;
            Toast.makeText(this, "已选择等宽字体", Toast.LENGTH_SHORT).show();
            fontDialog.dismiss();
        });
        
        btnFangSong.setOnClickListener(v -> {
            currentFontFamily = 4;
            Toast.makeText(this, "已选择仿宋字体", Toast.LENGTH_SHORT).show();
            fontDialog.dismiss();
        });
        
        btnKaiTi.setOnClickListener(v -> {
            currentFontFamily = 5;
            Toast.makeText(this, "已选择楷体字体", Toast.LENGTH_SHORT).show();
            fontDialog.dismiss();
        });
        
        btnClose.setOnClickListener(v -> fontDialog.dismiss());
        
        fontDialog.show();
    }
    
    private void showDirectionSelectDialog() {
        // 创建方向选择对话框
        Dialog directionDialog = new Dialog(this);
        directionDialog.setContentView(R.layout.dialog_direction_select);
        
        // 获取对话框中的控件
        Button btnHorizontal = directionDialog.findViewById(R.id.btnHorizontal);
        Button btnVertical = directionDialog.findViewById(R.id.btnVertical);
        Button btnClose = directionDialog.findViewById(R.id.btnCloseDirection);
        
        // 设置当前选中的方向
        if (currentTextDirection == 0) {
            btnHorizontal.setEnabled(false); // 已选中
            btnVertical.setEnabled(true);
        } else {
            btnHorizontal.setEnabled(true);
            btnVertical.setEnabled(false); // 已选中
        }
        
        // 设置按钮点击事件
        btnHorizontal.setOnClickListener(v -> {
            currentTextDirection = 0;
            Toast.makeText(this, "已选择横排", Toast.LENGTH_SHORT).show();
            directionDialog.dismiss();
        });
        
        btnVertical.setOnClickListener(v -> {
            currentTextDirection = 1;
            Toast.makeText(this, "已选择竖排", Toast.LENGTH_SHORT).show();
            directionDialog.dismiss();
        });
        
        btnClose.setOnClickListener(v -> directionDialog.dismiss());
        
        directionDialog.show();
    }

    private void showInspirationDialog() {
        Dialog inspirationDialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        inspirationDialog.setContentView(R.layout.dialog_ai_completion);
        
        // 初始化控件
        Button btnApiKey = inspirationDialog.findViewById(R.id.btnApiKey);
        EditText etPrompt = inspirationDialog.findViewById(R.id.etPrompt);
        Button btnComplete = inspirationDialog.findViewById(R.id.btnComplete);
        TextView tvCompletionResult = inspirationDialog.findViewById(R.id.tvCompletionResult);
        Button btnCloseCompletion = inspirationDialog.findViewById(R.id.btnCloseCompletion);
        
        // 设置按钮点击事件
        btnApiKey.setOnClickListener(v -> {
            // 显示API Key设置对话框
            showApiKeyDialog(inspirationDialog);
        });
        
        btnComplete.setOnClickListener(v -> {
            // 执行AI补全操作
            String userPrompt = etPrompt.getText().toString();
            String cardContent = etPoemContent.getText().toString();
            
            // 构建完整的提示词
            StringBuilder fullPrompt = new StringBuilder();
            fullPrompt.append("你是一位擅长写作中国古代诗词的大师");
            
            if (userPrompt.isEmpty()) {
                // 情况1：用户没有输入提示词
                fullPrompt.append("，请根据示例词牌补全这首诗词(注意请仅回复你创作的诗词本身，不需要创作思路)：");
                if (selectedCiPai != null) {
                    fullPrompt.append("词牌名: ").append(selectedCiPai.getName()).append(" ");
                }
                fullPrompt.append("根据下面的诗句补全");
                fullPrompt.append(cardContent);
            } else {
                // 情况2：用户输入了提示词
                fullPrompt.append("，").append(userPrompt).append("：").append(cardContent);
            }
            
            // 显示正在生成的提示
            tvCompletionResult.setText("正在生成...");
            
            // 调用DeepSeek API
            callDeepSeekAPI(fullPrompt.toString(), tvCompletionResult);
        });
        
        btnCloseCompletion.setOnClickListener(v -> inspirationDialog.dismiss());
        
        inspirationDialog.show();
    }
    
    private void showApiKeyDialog(Dialog parentDialog) {
        Dialog apiKeyDialog = new Dialog(this);
        apiKeyDialog.setContentView(R.layout.dialog_api_key);
        
        // 初始化控件
        EditText etApiKey = apiKeyDialog.findViewById(R.id.etApiKey);
        CheckBox cbRememberKey = apiKeyDialog.findViewById(R.id.cbRememberKey);
        Button btnSaveApiKey = apiKeyDialog.findViewById(R.id.btnSaveApiKey);
        Button btnCloseApiKey = apiKeyDialog.findViewById(R.id.btnCloseApiKey);
        Button btnTutorial = apiKeyDialog.findViewById(R.id.btnTutorial);
        
        // 加载已保存的API Key（如果有的话）
        String savedApiKey = settingsPrefs.getString("deepseek_api_key", "");
        if (!savedApiKey.isEmpty()) {
            etApiKey.setText(savedApiKey);
            cbRememberKey.setChecked(true);
        }
        
        // 设置按钮点击事件
        btnSaveApiKey.setOnClickListener(v -> {
            String apiKey = etApiKey.getText().toString();
            if (!apiKey.isEmpty()) {
                // 保存API Key（如果用户选择了记住）
                if (cbRememberKey.isChecked()) {
                    SharedPreferences.Editor editor = settingsPrefs.edit();
                    editor.putString("deepseek_api_key", apiKey);
                    editor.apply();
                }
                Toast.makeText(this, "API Key已保存", Toast.LENGTH_SHORT).show();
                apiKeyDialog.dismiss();
            } else {
                Toast.makeText(this, "请输入API Key", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnTutorial.setOnClickListener(v -> {
            // 显示API申请指南
            showApiGuideDialog();
        });
        
        btnCloseApiKey.setOnClickListener(v -> apiKeyDialog.dismiss());
        
        apiKeyDialog.show();
    }
    
    private void showApiGuideDialog() {
        Dialog guideDialog = new Dialog(this);
        guideDialog.setContentView(R.layout.dialog_api_guide);
        
        // 获取对话框中的控件
        TextView tvGuideContent = guideDialog.findViewById(R.id.tvGuideContent);
        Button btnCloseGuide = guideDialog.findViewById(R.id.btnCloseGuide);
        
        // 读取API指南内容
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.api_guide);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            inputStream.close();
            
            tvGuideContent.setText(content.toString());
        } catch (IOException e) {
            tvGuideContent.setText("无法加载API申请指南: " + e.getMessage());
        }
        
        // 设置关闭按钮点击事件
        btnCloseGuide.setOnClickListener(v -> guideDialog.dismiss());
        
        guideDialog.show();
    }
    
    private void saveWorkToMyWorks(String title, String author, String content, String background) {
        String userToken = preferencesManager.getToken();
        long userId = preferencesManager.getUserId();
        
        if (userToken == null || userToken.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 使用BackendService保存作品
        BackendService backendService = BackendService.getInstance(this);
        
        // 创建作品数据
        Map<String, Object> workData = new HashMap<>();
        workData.put("title", title);
        workData.put("content", content);
        workData.put("workType", "poem");
        
        // 添加背景信息
        Map<String, Object> backgroundInfo = new HashMap<>();
        backgroundInfo.put("type", "drawable");
        backgroundInfo.put("value", background);
        workData.put("backgroundInfo", backgroundInfo);
        
        // 调用BackendService保存作品
        BackendService.Result<Map<String, Object>> result = backendService.saveWork(workData, userId);
        
        if (result.getCode() == 200) {
            Toast.makeText(PoemComposeActivity.this, "作品保存成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PoemComposeActivity.this, "作品保存失败: " + result.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void callDeepSeekAPI(String prompt, TextView tvResult) {
        // 获取API密钥
        String apiKey = settingsPrefs.getString("deepseek_api_key", "");
        if (apiKey.isEmpty()) {
            runOnUiThread(() -> tvResult.setText("请先设置API Key"));
            return;
        }

        // 创建OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(45, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

        // ✅ 使用Gson构建JSON请求体（修复JSON相关错误）
        Gson gson = new Gson();
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "deepseek-reasoner");
        requestBody.addProperty("stream", true);
        requestBody.addProperty("return_reasoning", false);

        JsonArray messages = new JsonArray();

        // 系统消息
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "直接输出诗词正文，不需要思考过程");
        messages.add(systemMessage);

        // 用户消息
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 1000);

        // 创建请求
        MediaType JSON = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url("https://api.deepseek.com/v1/chat/completions")
                .post(RequestBody.create(JSON, gson.toJson(requestBody)))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        runOnUiThread(() -> tvResult.setText("正在生成诗词..."));

        // ✅ 修复所有Callback接口问题
        client.newCall(request).enqueue(new okhttp3.Callback() {
            private final StringBuilder contentBuilder = new StringBuilder();
            private long lastUpdateTime = 0;

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> tvResult.setText("网络错误: " + e.getMessage()));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                // ✅ 修复响应体访问错误
                if (!response.isSuccessful()) {
                    String errorBody = "无错误详情";
                    try {
                        if (response.body() != null) {
                            errorBody = response.body().string();
                        }
                    } catch (IOException e) {
                        // 忽略异常
                    }
                    final String finalError = errorBody;
                    runOnUiThread(() -> tvResult.setText("API错误: " + response.code() + "\n" + finalError));
                    return;
                }

                try {
                    // ✅ 修复输入流处理错误
                    InputStream inputStream = response.body().byteStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String jsonStr = line.substring(6).trim();

                            if ("[DONE]".equals(jsonStr)) {
                                showFinalResult();
                                return;
                            }

                            try {
                                // ✅ 修复JSON解析错误（使用正确方法）
                                JsonElement jsonElement = JsonParser.parseString(jsonStr);

                                if (jsonElement != null && jsonElement.isJsonObject()) {
                                    JsonObject json = jsonElement.getAsJsonObject();
                                    JsonArray choices = json.getAsJsonArray("choices");

                                    if (choices != null && choices.size() > 0) {
                                        JsonObject choice = choices.get(0).getAsJsonObject();
                                        JsonObject delta = choice.getAsJsonObject("delta");

                                        if (delta.has("content") && !delta.get("content").isJsonNull()) {
                                            String content = delta.get("content").getAsString();
                                            if (!content.isEmpty()) {
                                                contentBuilder.append(content);
                                                updateUI();
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 忽略JSON解析错误
                            }
                        }
                    }

                    showFinalResult();
                } catch (Exception e) {
                    runOnUiThread(() -> tvResult.setText("处理错误: " + e.getMessage()));
                }
            }

            private void updateUI() {
                final long now = System.currentTimeMillis();
                if (now - lastUpdateTime > 200) {
                    lastUpdateTime = now;
                    final String content = contentBuilder.toString();
                    runOnUiThread(() -> tvResult.setText(content));
                }
            }

            private void showFinalResult() {
                runOnUiThread(() -> {
                    String result = contentBuilder.toString().trim();
                    tvResult.setText(result.isEmpty() ? "生成完成，但未返回有效内容" : result);
                });
            }
        });
    }
    
    private void performSaveCardToLocal(TextView view) {
        try {
            // 创建Bitmap
            Bitmap bitmap = createBitmapFromView(view);
            
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
        // 确保在调用此方法前视图已经布局完成
        int width = view.getWidth();
        int height = view.getHeight();
        
        if (width <= 0 || height <= 0) {
            // 如果视图还没有测量，使用默认尺寸
            width = 800;
            height = 600;
        }
        
        // 创建与TextView相同尺寸的Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
    
    private String getFontName(int fontFamily) {
        switch (fontFamily) {
            case 0: 
                return "默认";
            case 1: 
                return "宋体";
            case 2: 
                return "黑体";
            case 3: 
                return "等宽";
            case 4: 
                return "仿宋";
            case 5: 
                return "楷体";
            default:
                return "默认";
        }
    }
}