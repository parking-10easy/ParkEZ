package com.parkez.user.exception;

import com.parkez.common.exception.ErrorCode;
import com.parkez.common.exception.ParkingEasyException;

public class InvalidUserRoleException extends ParkingEasyException {
    public InvalidUserRoleException() {
        super(null);
    }

    public InvalidUserRoleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
