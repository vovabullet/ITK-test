package ru.example.itktest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.example.itktest.model.Wallet;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findById(UUID id);

    /**
     * Атомарный SQL запрос на внесение денежной суммы
     * @param id кошелька
     * @param amount сумма
     * @return количество обновленных строк
     */
    @Modifying
    @Query("""
        UPDATE Wallet w
        SET w.amount = w.amount + :amount
        WHERE w.id = :id
    """)
    int deposit(@Param("id") UUID id, @Param("amount") BigDecimal amount);

    /**
     * Атомарный SQL запрос на снятие денежной суммы
     * @param id кошелька
     * @param amount сумма
     * @return количество обновленных строк
     */
    @Modifying
    @Query("""
        UPDATE Wallet w
        SET w.amount = w.amount - :amount
        WHERE w.id = :id
        AND w.amount >= :amount
    """)
    int withdraw(@Param("id") UUID id, @Param("amount") BigDecimal amount);
}
