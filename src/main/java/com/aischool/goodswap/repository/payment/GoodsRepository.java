    package com.aischool.goodswap.repository.payment;

    import jakarta.persistence.LockModeType;
    import jakarta.transaction.Transactional;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Lock;
    import org.springframework.data.jpa.repository.Modifying;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.stereotype.Repository;

    import com.aischool.goodswap.domain.Goods;

    @Repository
    public interface GoodsRepository extends JpaRepository<Goods, Long> {

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Transactional
        @Modifying
        @Query("UPDATE Goods g SET g.goodsStock = g.goodsStock - :quantity WHERE g.id = :id AND g.goodsStock >= :quantity")
        int updateGoodsQuantity(Long id, int quantity);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Transactional
        @Modifying
        @Query("UPDATE Goods g SET g.goodsStock = g.goodsStock + :quantity WHERE g.id = :id")
        void restoreGoodsQuantity(Long id, int quantity);

    }
