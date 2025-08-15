package com.example.poemai;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

public class PoemComposeActivity extends AppCompatActivity {
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
    private ImageButton btnRhyme, btnShare, btnSettings, btnInspiration, btnBack;
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
        Call<RhymeResponse> call = RetrofitClient.getInstance().getApiService().getRhymeWords(query);
        call.enqueue(new Callback<RhymeResponse>() {
            @Override
            public void onResponse(Call<RhymeResponse> call, retrofit2.Response<RhymeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RhymeResponse rhymeResponse = response.body();
                    if (rhymeResponse.getWords() != null && !rhymeResponse.getWords().isEmpty()) {
                        StringBuilder result = new StringBuilder();
                        result.append("韵脚字（韵母组: ").append(rhymeResponse.getRhymeGroup()).append("）:\n\n");
                        for (String word : rhymeResponse.getWords()) {
                            result.append(word).append("  ");
                        }
                        tvRhymeResults.setText(result.toString());
                    } else {
                        tvRhymeResults.setText("未找到相关韵脚字");
                    }
                } else {
                    try {
                        tvRhymeResults.setText("查询失败: " + response.errorBody().string());
                    } catch (Exception e) {
                        tvRhymeResults.setText("查询失败: " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<RhymeResponse> call, Throwable t) {
                tvRhymeResults.setText("网络错误: " + t.getMessage());
            }
        });
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
            case "elegant":
                tvSharePreview.setBackgroundResource(R.drawable.elegant_background);
                break;
            case "heroic":
                tvSharePreview.setBackgroundResource(R.drawable.heroic_background);
                break;
            case "blank":
                tvSharePreview.setBackgroundResource(R.drawable.blank_background);
                break;
            case "red_solid":
                tvSharePreview.setBackgroundResource(R.drawable.red_solid_background);
                break;
            case "dark_solid":
                tvSharePreview.setBackgroundResource(R.drawable.dark_solid_background);
                break;
        }
        
        // 设置按钮点击事件
        btnSelectBackground.setOnClickListener(v -> {
            // 显示背景选择对话框
            showBackgroundSelectDialog(shareDialog);
        });
        
        btnSaveToLocal.setOnClickListener(v -> {
            Toast.makeText(this, "保存到本地功能待实现", Toast.LENGTH_SHORT).show();
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
        Button btnElegant = backgroundDialog.findViewById(R.id.btnElegant);
        Button btnHeroic = backgroundDialog.findViewById(R.id.btnHeroic);
        Button btnBlank = backgroundDialog.findViewById(R.id.btnBlank);
        Button btnRedSolid = backgroundDialog.findViewById(R.id.btnRedSolid);
        Button btnDarkSolid = backgroundDialog.findViewById(R.id.btnDarkSolid);
        Button btnImportBackground = backgroundDialog.findViewById(R.id.btnImportBackground);
        Button btnCloseBackground = backgroundDialog.findViewById(R.id.btnCloseBackground);
        
        // 设置按钮点击事件
        btnElegant.setOnClickListener(v -> {
            Toast.makeText(this, "已选择婉约风格背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "elegant");
            editor.apply();
            backgroundDialog.dismiss();
        });
        
        btnHeroic.setOnClickListener(v -> {
            Toast.makeText(this, "已选择豪放风格背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "heroic");
            editor.apply();
            backgroundDialog.dismiss();
        });
        
        btnBlank.setOnClickListener(v -> {
            Toast.makeText(this, "已选择空白风格背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "blank");
            editor.apply();
            backgroundDialog.dismiss();
        });
        
        btnRedSolid.setOnClickListener(v -> {
            Toast.makeText(this, "已选择朱红纯色背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "red_solid");
            editor.apply();
            backgroundDialog.dismiss();
        });
        
        btnDarkSolid.setOnClickListener(v -> {
            Toast.makeText(this, "已选择淡黑纯色背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "dark_solid");
            editor.apply();
            backgroundDialog.dismiss();
        });
        
        btnImportBackground.setOnClickListener(v -> {
            Toast.makeText(this, "导入外部图片功能待实现", Toast.LENGTH_SHORT).show();
            backgroundDialog.dismiss();
        });
        
        btnCloseBackground.setOnClickListener(v -> backgroundDialog.dismiss());
        
        backgroundDialog.show();
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
        // 创建作品数据
        Map<String, Object> workData = new HashMap<>();
        workData.put("title", title);
        workData.put("content", content);
        workData.put("workType", "template_poem");
        
        // 添加字体设置
        Map<String, Object> fontSetting = new HashMap<>();
        String fontName = getFontName(currentFontFamily);
        fontSetting.put("font", fontName);
        fontSetting.put("size", currentFontSize);
        fontSetting.put("color", "#000000"); // 默认黑色
        workData.put("fontSetting", fontSetting);
        
        // 添加背景信息
        Map<String, Object> backgroundInfo = new HashMap<>();
        backgroundInfo.put("type", "drawable");
        backgroundInfo.put("value", background);
        workData.put("backgroundInfo", backgroundInfo);
        
        // 获取用户token
        String userToken = preferencesManager.getToken();
        if (userToken == null || userToken.isEmpty()) {
            Toast.makeText(this, "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 调用API保存作品
        Call<WorkSaveResponse> call = RetrofitClient.getInstance().getApiService().saveWork("Bearer " + userToken, workData);
        call.enqueue(new Callback<WorkSaveResponse>() {
            @Override
            public void onResponse(Call<WorkSaveResponse> call, retrofit2.Response<WorkSaveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WorkSaveResponse result = response.body();
                    if (result.getCode() == 200 && result.getId() != null) {
                        Toast.makeText(PoemComposeActivity.this, "作品保存成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PoemComposeActivity.this, "作品保存失败: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PoemComposeActivity.this, "作品保存失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WorkSaveResponse> call, Throwable t) {
                Toast.makeText(PoemComposeActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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