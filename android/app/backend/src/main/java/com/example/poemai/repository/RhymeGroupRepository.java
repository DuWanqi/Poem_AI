package com.example.poemai.repository;

import com.example.poemai.model.RhymeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// import java.util.List;
import java.util.Optional;

@Repository
public interface RhymeGroupRepository extends JpaRepository<RhymeGroup, Long> {

    // 根据字查找押韵组
    // @Query(value = "SELECT * FROM rhyme_group WHERE character_list ? :ch", nativeQuery = true)
    // Optional<RhymeGroup> findByCharacter(String ch);
    @Query(
    value = "SELECT * FROM rhyme_group WHERE jsonb_exists(character_list, :ch)", 
    nativeQuery = true
    )
    Optional<RhymeGroup> findByCharacter(@Param("ch") String ch);
}