package com.example.poemai;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.poemai.utils.PreferencesManager;
import com.example.poemai.service.BackendService;
import com.example.poemai.model.LoginResponse;
import com.example.poemai.model.WorkSaveResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardCreateActivity extends AppCompatActivity {
    private static final String TAG = "CardCreateActivity";
    private static final String PREFS_NAME = "CardCreateSettings";
    private static final String FONT_SIZE_KEY = "font_size";
    private static final String TEXT_COLOR_KEY = "text_color";
    private static final String TEXT_DIRECTION_KEY = "text_direction";
    private static final String FONT_FAMILY_KEY = "font_family";
    
    private VerticalTextView etCardContent;
    private Button btnCreatePoem, btnRecommendCiPai, btnSaveCard;
    private ImageButton btnSettings, btnBack;
    private PreferencesManager preferencesManager;
    private BackendService backendService; // 使用BackendService而不是Retrofit
    private SharedPreferences settingsPrefs;
    
    // 默认设置值
    private int currentFontSize = 16;
    private int currentTextColor = R.color.classical_text;
    private int currentTextDirection = 0; // 0 for horizontal, 1 for vertical
    private int currentFontFamily = 0; // 0 for default, 1 for 宋体(Serif), 2 for 黑体(Sans Serif), 3 for 等宽字体(Monospace), 4 for 仿宋(FangSong), 5 for 楷体(KaiTi)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_create);
        
        Log.d(TAG, "onCreate: CardCreateActivity started");
        
        // 确保软键盘可以正常显示
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | 
                                   WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        preferencesManager = new PreferencesManager(this);
        backendService = BackendService.getInstance(this); // 初始化BackendService
        settingsPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // 加载保存的设置
        loadSettings();
        
        initViews();
        setupListeners();
        
        // 应用设置
        applySettings();
        
        // 检查是否有传入的内容需要显示
        loadIncomingContent();
        
        // 确保EditText能够获取焦点并唤起键盘
        etCardContent.requestFocus();
        
        Log.d(TAG, "onCreate: EditText focusable=" + etCardContent.isFocusable() + 
              ", focusableInTouchMode=" + etCardContent.isFocusableInTouchMode() + 
              ", clickable=" + etCardContent.isClickable() + 
              ", enabled=" + etCardContent.isEnabled());
        
        // 立即尝试显示键盘
        showKeyboard();
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
        etCardContent.setTextSize(currentFontSize);
        
        // 应用文本颜色
        etCardContent.setTextColor(ContextCompat.getColor(this, currentTextColor));
        
        // 应用文本方向
        applyTextDirection();
        
        // 应用字体
        applyFontFamily();
    }
    
    private void applyFontFamily() {
        switch (currentFontFamily) {
            case 0: // 默认系统字体
                etCardContent.setTypeface(Typeface.DEFAULT);
                break;
            case 1: // 宋体（Serif）
                etCardContent.setTypeface(Typeface.SERIF);
                break;
            case 2: // 黑体（Sans Serif）
                etCardContent.setTypeface(Typeface.SANS_SERIF);
                break;
            case 3: // 等宽字体
                etCardContent.setTypeface(Typeface.MONOSPACE);
                break;
            case 4: // 仿宋字体（如果可用）
                etCardContent.setTypeface(Typeface.create("fangsong", Typeface.NORMAL));
                break;
            case 5: // 楷体字体（如果可用）
                etCardContent.setTypeface(Typeface.create("kaiti", Typeface.NORMAL));
                break;
            default:
                etCardContent.setTypeface(Typeface.DEFAULT);
                break;
        }
    }
    
    private void applyTextDirection() {
        // 横排
        if (currentTextDirection == 0) {
            etCardContent.setVertical(false);
        } 
        // 竖排
        else {
            etCardContent.setVertical(true);
        }
    }

    private void showKeyboard() {
        etCardContent.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                // 简化键盘显示逻辑
                imm.showSoftInput(etCardContent, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etCardContent.getWindowToken(), 0);
        }
    }

    private void initViews() {
        etCardContent = findViewById(R.id.etCardContent);
        btnRecommendCiPai = findViewById(R.id.btnRecommendCiPai);
        btnSaveCard = findViewById(R.id.btnSaveCard);
        btnSettings = findViewById(R.id.btnSettings);
        btnBack = findViewById(R.id.btnBack);
        
        // 确保 EditText 能够获取焦点
        etCardContent.setFocusable(true);
        etCardContent.setFocusableInTouchMode(true);
        etCardContent.setClickable(true);
        
        Log.d(TAG, "initViews: Views initialized");
    }

    private void setupListeners() {
        // 添加返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());

        // 移除了创建诗词按钮的监听器，因为该功能暂时未使用

        btnRecommendCiPai.setOnClickListener(v -> {
            String content = etCardContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入卡片内容", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 分析内容长度以推荐词牌
            List<List<Integer>> lengths = analyzeContentLengths(content);
            
            Intent intent = new Intent(CardCreateActivity.this, CiPaiRecommendActivity.class);
            intent.putExtra("content", content);
            
            // 传递长度信息
            intent.putExtra("lengths", new ArrayList<>(lengths)); // 简化处理
            startActivity(intent);
        });

        btnSaveCard.setOnClickListener(v -> saveCard());
    
        btnSettings.setOnClickListener(v -> {
            hideKeyboard();
            showSettingsDialog();
        });
        
        // 为EditText添加点击事件，确保点击时能唤起键盘
        etCardContent.setOnClickListener(v -> {
            etCardContent.requestFocus();
            showKeyboard();
        });
        
        // 当EditText获得焦点时显示键盘
        etCardContent.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etCardContent.postDelayed(() -> showKeyboard(), 300);
            }
        });
    
        // 处理文本变化事件
        etCardContent.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 可以保留必要的日志
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 文本正在变化
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // 文本变化后
            }
        });
    }

    private List<List<Integer>> analyzeContentLengths(String content) {
        List<List<Integer>> lengths = new ArrayList<>();
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                List<Integer> lineLengths = new ArrayList<>();
                lineLengths.add(line.trim().length());
                lengths.add(lineLengths);
            }
        }
        
        return lengths;
    }
    
    private void loadIncomingContent() {
        // 通过JSON字符串传递数据
        String workDataJson = getIntent().getStringExtra("work_data_json");
        if (workDataJson != null) {
            Gson gson = new Gson();
            Map<String, Object> workData = gson.fromJson(workDataJson, Map.class);
            
            if (workData != null) {
                String content = (String) workData.get("content");
                if (content != null) {
                    etCardContent.setText(content);
                }
            }
        }
    }
    
    private void showSettingsDialog() {
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_settings);
        
        // 初始化设置对话框中的控件
        Button btnImportFont = settingsDialog.findViewById(R.id.btnImportFont);
        Button btnFontSizeDecrease = settingsDialog.findViewById(R.id.btnFontSizeDecrease);
        Button btnFontSizeIncrease = settingsDialog.findViewById(R.id.btnFontSizeIncrease);
        TextView tvFontSize = settingsDialog.findViewById(R.id.tvFontSize);
        RadioGroup radioGroupDirection = settingsDialog.findViewById(R.id.radioGroupDirection);
        RadioButton radioHorizontal = settingsDialog.findViewById(R.id.radioHorizontal);
        RadioButton radioVertical = settingsDialog.findViewById(R.id.radioVertical);
        Button btnColorBlack = settingsDialog.findViewById(R.id.btnColorBlack);
        Button btnColorWhite = settingsDialog.findViewById(R.id.btnColorWhite);
        Button btnColorGold = settingsDialog.findViewById(R.id.btnColorGold);
        Button btnColorDarkRed = settingsDialog.findViewById(R.id.btnColorDarkRed);
        Button btnSelectDirection = settingsDialog.findViewById(R.id.btnSelectDirection);
        Button btnCloseSettings = settingsDialog.findViewById(R.id.btnCloseSettings);
        
        // 设置当前字体大小显示
        tvFontSize.setText(String.valueOf(currentFontSize));
        
        // 设置当前文本方向选择
        if (currentTextDirection == 0) {
            radioHorizontal.setChecked(true);
        } else {
            radioVertical.setChecked(true);
        }
        
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
        
        radioGroupDirection.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioHorizontal) {
                currentTextDirection = 0;
                Toast.makeText(this, "已选择横排", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.radioVertical) {
                currentTextDirection = 1;
                Toast.makeText(this, "已选择竖排", Toast.LENGTH_SHORT).show();
            }
        });
        
        View.OnClickListener colorClickListener = v -> {
            if (v == btnColorBlack) {
                currentTextColor = android.R.color.black;
                Toast.makeText(this, "已选择黑色字体", Toast.LENGTH_SHORT).show();
            } else if (v == btnColorWhite) {
                currentTextColor = android.R.color.white;
                Toast.makeText(this, "已选择白色字体", Toast.LENGTH_SHORT).show();
            } else if (v == btnColorGold) {
                currentTextColor = R.color.gold; // 需要在colors.xml中定义
                Toast.makeText(this, "已选择金色字体", Toast.LENGTH_SHORT).show();
            } else if (v == btnColorDarkRed) {
                currentTextColor = R.color.dark_red; // 需要在colors.xml中定义
                Toast.makeText(this, "已选择深红色字体", Toast.LENGTH_SHORT).show();
            }
        };
        
        btnColorBlack.setOnClickListener(colorClickListener);
        btnColorWhite.setOnClickListener(colorClickListener);
        btnColorGold.setOnClickListener(colorClickListener);
        btnColorDarkRed.setOnClickListener(colorClickListener);
        
        btnImportFont.setOnClickListener(v -> {
            // 显示字体选择对话框
            showFontSelectDialog();
        });
        
        btnSelectDirection.setOnClickListener(v -> {
            // 显示方向选择对话框
            showDirectionSelectDialog();
        });
        
        btnCloseSettings.setOnClickListener(v -> {
            settingsDialog.dismiss();
            // 保存设置
            saveSettings();
            // 应用设置
            applySettings();
            etCardContent.postDelayed(() -> {
                etCardContent.requestFocus();
                showKeyboard();
            }, 100);
        });
        
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

    private void saveCard() {
        String content = etCardContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入卡片内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构造作品数据
        Map<String, Object> workData = new HashMap<>();
        workData.put("title", "卡片作品");
        workData.put("content", content);
        workData.put("workType", "raw_card");

        // 获取用户ID
        long userId = preferencesManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 使用BackendService保存作品
        BackendService.Result<Map<String, Object>> result = backendService.saveWork(workData, userId);
        if (result.getCode() == 200 && result.getData() != null) {
            Toast.makeText(CardCreateActivity.this, "卡片保存成功", Toast.LENGTH_SHORT).show();
            // 保存成功后，设置结果并关闭当前Activity，以便MainActivity刷新
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(CardCreateActivity.this, "保存失败: " + result.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 当Activity恢复时，确保EditText有焦点
        etCardContent.postDelayed(() -> {
            etCardContent.requestFocus();
            showKeyboard();
        }, 200);
    }
}