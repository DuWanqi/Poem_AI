-- 开发环境专属
DROP TABLE IF EXISTS my_works CASCADE;
DROP TABLE IF EXISTS cipai CASCADE;
DROP TABLE IF EXISTS rhyme_group CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS api_keys CASCADE;


-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 添加字段以支持用户禁用和锁定
ALTER TABLE users
  ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT true,
  ADD COLUMN account_locked BOOLEAN NOT NULL DEFAULT false;
-- 词牌表
-- src/main/resources/schema.sql

-- DROP TABLE IF EXISTS cipai;

CREATE TABLE IF NOT EXISTS cipai (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    example_text TEXT,
    sentence_lengths TEXT[]
);

-- 创建 GIN 索引以加速 JSONB 查询
-- CREATE INDEX idx_cipai_sentence_lengths ON cipai USING GIN (sentence_lengths);
CREATE INDEX IF NOT EXISTS idx_cipai_sentence_lengths ON cipai USING GIN (sentence_lengths);
-- 押韵组表
CREATE TABLE IF NOT EXISTS rhyme_group (
    id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(50) NOT NULL, -- 如 "(ong,iong)"
    character_list JSONB -- 包含该组所有汉字的列表
);
-- 创建索引加速单字查询
CREATE INDEX IF NOT EXISTS idx_rhyme_group ON rhyme_group USING GIN (character_list jsonb_path_ops);

-- 作品表
CREATE TABLE IF NOT EXISTS my_works (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255), -- 可为空
    content TEXT NOT NULL, -- 诗词内容
    work_type VARCHAR(50), -- 类型: raw_card, template_poem, shared_image
    font_setting JSONB, -- 字体设置 (字体、大小、颜色等)
    background_info JSONB, -- 背景信息 (图片路径或颜色)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    associated_cipai_id BIGINT REFERENCES cipai(id), -- 关联词牌
    template_highlight_index JSONB -- 模板中高亮部分索引
);

-- API Key 表（可选）
CREATE TABLE IF NOT EXISTS api_keys (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    encrypted_key TEXT NOT NULL, -- 加密后的 Key
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_user_id ON my_works(user_id);
CREATE INDEX IF NOT EXISTS idx_cipai_id ON my_works(associated_cipai_id);
CREATE INDEX IF NOT EXISTS idx_group_name ON rhyme_group(group_name);
CREATE UNIQUE INDEX IF NOT EXISTS idx_username ON users(username);