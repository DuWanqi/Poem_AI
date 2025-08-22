package com.example.poemai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// import java.util.List;

@Entity
@Table(name = "cipai")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CiPai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 词牌名称，例如“水调歌头”

    @Column(columnDefinition = "TEXT")
    private String exampleText; // 示例诗句

    // @Column(columnDefinition = "JSONB")
    // private List<Integer> sentenceLengths; // 每句字数列表
    // 关键修改：改为 String[] 类型以匹配数据库的 text[] 存储
    @Column(name = "sentence_lengths", columnDefinition = "text[]")
    private String[] sentenceLengths; // 存储格式: {"[7,5]", "[7]", "[5,5]"}
}