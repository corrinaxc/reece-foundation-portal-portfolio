package com.reecefoundation.reecefoundation.repository;

import com.reecefoundation.reecefoundation.model.Payment;
import com.reecefoundation.reecefoundation.model.Project;
import com.reecefoundation.reecefoundation.model.ProjectBankDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface PaymentDateRepository extends JpaRepository <Payment, Long> {

    List<Payment> findAllByOrderByIdDesc();

    List<Payment> findByProject_Organisation_IdOrderByPaymentDueDateDesc(Long organisationId);

    boolean existsByProjectIdAndPaymentAmountAndPaymentDueDate(Long projectId, BigDecimal paymentAmount, Date paymentDueDate);

    @Query("SELECT p FROM Payment p WHERE EXTRACT(MONTH FROM p.paymentDueDate) = :month AND EXTRACT(YEAR FROM p.paymentDueDate) = :year ORDER BY p.id DESC")
    List<Payment> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT p FROM Payment p WHERE (p.paid IS NULL OR p.paid = false) ORDER BY p.id DESC")
    List<Payment> findAllUnpaid();

    @Query("SELECT p FROM Payment p WHERE EXTRACT(MONTH FROM p.paymentDueDate) = :month AND EXTRACT(YEAR FROM p.paymentDueDate) = :year AND (p.paid IS NULL OR p.paid = false) ORDER BY p.id DESC")
    List<Payment> findUnpaidByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
