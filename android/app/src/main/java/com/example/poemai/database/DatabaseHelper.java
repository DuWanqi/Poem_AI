package com.example.poemai.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.poemai.service.BackendService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "PoemAi.db";
    private static final int DATABASE_VERSION = 1;
    
    // 用户表
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_ACCOUNT_LOCKED = "account_locked";
    
    // 作品表
    private static final String TABLE_WORKS = "my_works";
    private static final String COLUMN_WORK_ID = "id";
    private static final String COLUMN_USER_ID_FK = "user_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_WORK_TYPE = "work_type";
    private static final String COLUMN_FONT_SETTING = "font_setting";
    private static final String COLUMN_BACKGROUND_INFO = "background_info";
    private static final String COLUMN_CREATED_AT_WORK = "created_at";
    private static final String COLUMN_UPDATED_AT_WORK = "updated_at";
    private static final String COLUMN_ASSOCIATED_CIPAI_ID = "associated_cipai_id";
    private static final String COLUMN_TEMPLATE_HIGHLIGHT_INDEX = "template_highlight_index";
    
    private Gson gson;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.gson = new Gson();
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        String createUserTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                COLUMN_UPDATED_AT + " INTEGER NOT NULL, " +
                COLUMN_ENABLED + " INTEGER DEFAULT 1, " +
                COLUMN_ACCOUNT_LOCKED + " INTEGER DEFAULT 0)";
        db.execSQL(createUserTable);
        
        // 创建作品表
        String createWorkTable = "CREATE TABLE " + TABLE_WORKS + " (" +
                COLUMN_WORK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID_FK + " INTEGER NOT NULL, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT NOT NULL, " +
                COLUMN_WORK_TYPE + " TEXT, " +
                COLUMN_FONT_SETTING + " TEXT, " +
                COLUMN_BACKGROUND_INFO + " TEXT, " +
                COLUMN_CREATED_AT_WORK + " INTEGER NOT NULL, " +
                COLUMN_UPDATED_AT_WORK + " INTEGER NOT NULL, " +
                COLUMN_ASSOCIATED_CIPAI_ID + " INTEGER, " +
                COLUMN_TEMPLATE_HIGHLIGHT_INDEX + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        db.execSQL(createWorkTable);
        
        Log.d(TAG, "Database tables created");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
    // 用户相关操作
    public long insertUser(BackendService.User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD_HASH, user.getPasswordHash());
        
        long id = db.insert(TABLE_USERS, null, values);
        user.setId(id);
        db.close();
        return id;
    }
    
    public BackendService.User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_PASSWORD_HASH};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        BackendService.User user = null;
        
        if (cursor != null && cursor.moveToFirst()) {
            user = new BackendService.User();
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)));
        }
        
        if (cursor != null) cursor.close();
        db.close();
        return user;
    }
    
    public BackendService.User getUserById(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_PASSWORD_HASH};
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        BackendService.User user = null;
        
        if (cursor != null && cursor.moveToFirst()) {
            user = new BackendService.User();
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)));
        }
        
        if (cursor != null) cursor.close();
        db.close();
        return user;
    }
    
    // 作品相关操作
    public long insertWork(Work work) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID_FK, work.getUserId());
        values.put(COLUMN_TITLE, work.getTitle());
        values.put(COLUMN_CONTENT, work.getContent());
        values.put(COLUMN_WORK_TYPE, work.getWorkType());
        values.put(COLUMN_FONT_SETTING, work.getFontSetting() != null ? gson.toJson(work.getFontSetting()) : null);
        values.put(COLUMN_BACKGROUND_INFO, work.getBackgroundInfo() != null ? gson.toJson(work.getBackgroundInfo()) : null);
        values.put(COLUMN_CREATED_AT_WORK, System.currentTimeMillis());
        values.put(COLUMN_UPDATED_AT_WORK, System.currentTimeMillis());
        values.put(COLUMN_ASSOCIATED_CIPAI_ID, work.getAssociatedCipaiId());
        values.put(COLUMN_TEMPLATE_HIGHLIGHT_INDEX, work.getTemplateHighlightIndex() != null ? gson.toJson(work.getTemplateHighlightIndex()) : null);
        
        long id = db.insert(TABLE_WORKS, null, values);
        work.setId(id);
        db.close();
        return id;
    }
    
    public List<Work> getWorksByUserId(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_WORK_ID, COLUMN_USER_ID_FK, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_WORK_TYPE,
                COLUMN_FONT_SETTING, COLUMN_BACKGROUND_INFO, COLUMN_CREATED_AT_WORK, COLUMN_UPDATED_AT_WORK,
                COLUMN_ASSOCIATED_CIPAI_ID, COLUMN_TEMPLATE_HIGHLIGHT_INDEX};
        String selection = COLUMN_USER_ID_FK + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = db.query(TABLE_WORKS, columns, selection, selectionArgs, null, null, COLUMN_UPDATED_AT_WORK + " DESC");
        List<Work> works = new ArrayList<>();
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Work work = new Work();
                work.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_WORK_ID)));
                work.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID_FK)));
                work.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                work.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                work.setWorkType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORK_TYPE)));
                
                String fontSettingStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FONT_SETTING));
                if (fontSettingStr != null) {
                    Map<String, Object> fontSetting = gson.fromJson(fontSettingStr, new TypeToken<Map<String, Object>>(){}.getType());
                    work.setFontSetting(fontSetting);
                }
                
                String backgroundInfoStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BACKGROUND_INFO));
                if (backgroundInfoStr != null) {
                    Map<String, Object> backgroundInfo = gson.fromJson(backgroundInfoStr, new TypeToken<Map<String, Object>>(){}.getType());
                    work.setBackgroundInfo(backgroundInfo);
                }
                
                work.setCreatedAt(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT_WORK))));
                work.setUpdatedAt(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT_WORK))));
                
                long associatedCipaiId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ASSOCIATED_CIPAI_ID));
                if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_ASSOCIATED_CIPAI_ID))) {
                    work.setAssociatedCipaiId(associatedCipaiId);
                }
                
                String templateHighlightIndexStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEMPLATE_HIGHLIGHT_INDEX));
                if (templateHighlightIndexStr != null) {
                    Map<String, Object> templateHighlightIndex = gson.fromJson(templateHighlightIndexStr, new TypeToken<Map<String, Object>>(){}.getType());
                    work.setTemplateHighlightIndex(templateHighlightIndex);
                }
                
                works.add(work);
            } while (cursor.moveToNext());
        }
        
        if (cursor != null) cursor.close();
        db.close();
        return works;
    }
    
    // 添加getAllWorksByUserId方法作为getWorksByUserId的别名
    public List<Work> getAllWorksByUserId(long userId) {
        return getWorksByUserId(userId);
    }
    
    public Work getWorkById(long workId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_WORK_ID, COLUMN_USER_ID_FK, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_WORK_TYPE,
                COLUMN_FONT_SETTING, COLUMN_BACKGROUND_INFO, COLUMN_CREATED_AT_WORK, COLUMN_UPDATED_AT_WORK,
                COLUMN_ASSOCIATED_CIPAI_ID, COLUMN_TEMPLATE_HIGHLIGHT_INDEX};
        String selection = COLUMN_WORK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(workId)};
        
        Cursor cursor = db.query(TABLE_WORKS, columns, selection, selectionArgs, null, null, null);
        Work work = null;
        
        if (cursor != null && cursor.moveToFirst()) {
            work = new Work();
            work.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_WORK_ID)));
            work.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID_FK)));
            work.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
            work.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
            work.setWorkType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORK_TYPE)));
            
            String fontSettingStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FONT_SETTING));
            if (fontSettingStr != null) {
                Map<String, Object> fontSetting = gson.fromJson(fontSettingStr, new TypeToken<Map<String, Object>>(){}.getType());
                work.setFontSetting(fontSetting);
            }
            
            String backgroundInfoStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BACKGROUND_INFO));
            if (backgroundInfoStr != null) {
                Map<String, Object> backgroundInfo = gson.fromJson(backgroundInfoStr, new TypeToken<Map<String, Object>>(){}.getType());
                work.setBackgroundInfo(backgroundInfo);
            }
            
            work.setCreatedAt(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT_WORK))));
            work.setUpdatedAt(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT_WORK))));
            
            long associatedCipaiId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ASSOCIATED_CIPAI_ID));
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_ASSOCIATED_CIPAI_ID))) {
                work.setAssociatedCipaiId(associatedCipaiId);
            }
            
            String templateHighlightIndexStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEMPLATE_HIGHLIGHT_INDEX));
            if (templateHighlightIndexStr != null) {
                Map<String, Object> templateHighlightIndex = gson.fromJson(templateHighlightIndexStr, new TypeToken<Map<String, Object>>(){}.getType());
                work.setTemplateHighlightIndex(templateHighlightIndex);
            }
        }
        
        if (cursor != null) cursor.close();
        db.close();
        return work;
    }
    
    public int deleteWorkByIdAndUserId(long workId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_WORK_ID + " = ? AND " + COLUMN_USER_ID_FK + " = ?";
        String[] selectionArgs = {String.valueOf(workId), String.valueOf(userId)};
        
        int deletedRows = db.delete(TABLE_WORKS, selection, selectionArgs);
        db.close();
        return deletedRows;
    }
    
    // 作品类
    public static class Work {
        private Long id;
        private Long userId;
        private String title;
        private String content;
        private String workType;
        private Map<String, Object> fontSetting;
        private Map<String, Object> backgroundInfo;
        private Date createdAt;
        private Date updatedAt;
        private Long associatedCipaiId;
        private Map<String, Object> templateHighlightIndex;
        
        // Constructors
        public Work() {
            this.createdAt = new Date();
            this.updatedAt = new Date();
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getWorkType() { return workType; }
        public void setWorkType(String workType) { this.workType = workType; }
        
        public Map<String, Object> getFontSetting() { return fontSetting; }
        public void setFontSetting(Map<String, Object> fontSetting) { this.fontSetting = fontSetting; }
        
        public Map<String, Object> getBackgroundInfo() { return backgroundInfo; }
        public void setBackgroundInfo(Map<String, Object> backgroundInfo) { this.backgroundInfo = backgroundInfo; }
        
        public Date getCreatedAt() { return createdAt; }
        public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
        
        public Date getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
        
        public Long getAssociatedCipaiId() { return associatedCipaiId; }
        public void setAssociatedCipaiId(Long associatedCipaiId) { this.associatedCipaiId = associatedCipaiId; }
        
        public Map<String, Object> getTemplateHighlightIndex() { return templateHighlightIndex; }
        public void setTemplateHighlightIndex(Map<String, Object> templateHighlightIndex) { this.templateHighlightIndex = templateHighlightIndex; }
    }
}