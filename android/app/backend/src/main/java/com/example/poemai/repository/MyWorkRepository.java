package com.example.poemai.repository;

import com.example.poemai.model.MyWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 关键修复：添加Query导入
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyWorkRepository extends JpaRepository<MyWork, Long> {
    
    // 查询某个用户的所有作品
    List<MyWork> findByUserId(Long userId);

    // 查询某个用户并按时间排序
    @Query("SELECT w FROM MyWork w WHERE w.userId = :userId ORDER BY w.updatedAt DESC")
    List<MyWork> findRecentWorksByUserId(Long userId);
}