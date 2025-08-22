package com.example.poemai.repository;

import com.example.poemai.model.CiPai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
// public interface CiPaiRepository extends JpaRepository<CiPai, Long> {

//     // 原始方法（基于JPA自动生成）
//     List<CiPai> findBySentenceLengths(List<Integer> lengths);

//     // 可选：显式写SQL查询，用于更复杂的匹配逻辑
//     @Query(value = "SELECT * FROM cipai WHERE sentence_lengths @> :lengths::jsonb", nativeQuery = true)
//     List<CiPai> findMatchingCipai(List<Integer> lengths);
// }
public interface CiPaiRepository extends JpaRepository<CiPai, Long> {

    @Query(value = """
SELECT * FROM cipai 
WHERE :searchText = ANY(sentence_lengths)
""", nativeQuery = true)
List<CiPai> findBySentenceArray(@Param("searchText") String searchText);
    
}