package com.chenpeiyu.dto;


import com.chenpeiyu.entity.OrderDetail;
import com.chenpeiyu.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;
//
    //订单的总的件数
    private int sumNum;

    private List<OrderDetail> orderDetails;
	
}
