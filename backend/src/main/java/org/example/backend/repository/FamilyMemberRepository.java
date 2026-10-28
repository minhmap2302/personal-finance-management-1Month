package org.example.backend.repository;

import org.example.backend.model.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    Optional<FamilyMember> findByUserIdAndFamilyId(Long userId, Long familyId);
}