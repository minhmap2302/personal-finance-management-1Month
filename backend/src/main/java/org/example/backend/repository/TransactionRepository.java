package org.example.backend.repository;

import org.example.backend.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Bản tối thiểu, chỉ có phần cần cho việc xóa gia đình. Gộp với repository giao dịch khi viết module giao dịch.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByCategoryId(Long categoryId);
 
    /** Xóa mọi giao dịch của gia đình (bước đầu tiên khi xóa gia đình). */
    @Modifying(flushAutomatically = true)
    @Query("delete from Transaction t where t.family.id = :familyId")
    int deleteAllByFamilyId(@Param("familyId") Long familyId);
}