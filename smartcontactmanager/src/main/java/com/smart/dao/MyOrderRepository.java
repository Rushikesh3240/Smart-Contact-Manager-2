package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entities.MyOrders;

public interface MyOrderRepository extends JpaRepository<MyOrders,Long>{
    public MyOrders findByOrderId(String orderId);

}
