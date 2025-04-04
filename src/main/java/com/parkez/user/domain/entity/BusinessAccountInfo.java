package com.parkez.user.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class BusinessAccountInfo {

    private final String businessNumber;
    private final String depositorName;
    private final String bankName;
    private final String bankAccount;

    protected BusinessAccountInfo() {
        this.businessNumber = null;
        this.depositorName = null;
        this.bankName = null;
        this.bankAccount = null;
    }

    public BusinessAccountInfo(String businessNumber, String depositorName, String bankName, String bankAccount) {
        this.businessNumber = businessNumber;
        this.depositorName = depositorName;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }

}
