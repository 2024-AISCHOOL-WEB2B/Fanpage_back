    package com.aischool.goodswap.domain;

    import jakarta.persistence.*;
    import org.hibernate.annotations.CreationTimestamp;

    import lombok.Getter;
    import lombok.Builder;
    import lombok.ToString;
    import lombok.AccessLevel;
    import lombok.NoArgsConstructor;

    import java.time.LocalDateTime;

    @Getter
    @Entity
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @ToString
    @Table(name = "tb_orderTemporal")
    public class OrderTemporal {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "od_idx")
        private Long id;

        @Column(name = "merchant_uid", nullable = false)
        private String merchantUid;

        @Column(name = "goods", nullable = false)
        private Long goods;

        @Column(name = "quantity", nullable = false) // 구매 수량
        private int quantity;

        @Column(name = "amount", nullable = false)
        private int amount;

        @Column(name = "discount_amount", nullable = false)
        private int discountAmount;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

        @Builder
        public OrderTemporal(String merchantUid, Long goods, int quantity, int amount, int discountAmount) {
            this.merchantUid = merchantUid;
            this.goods = goods;
            this.quantity = quantity;
            this.amount = amount;
            this.discountAmount = discountAmount;
        }
    }
